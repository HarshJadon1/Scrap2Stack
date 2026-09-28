package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.core.network.toUserFriendlyMessage
import com.scrap2stack.app.data.mapper.*
import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.domain.repository.CollaborationRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class CollaborationRepositoryImpl : CollaborationRepository {

    override suspend fun sendCollaborationRequest(
        projectId: String,
        receiverId: String,
        message: String
    ): Result<CollaborationRequest> = withContext(Dispatchers.IO) {
        try {
            val senderId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            if (receiverId.isBlank()) {
                return@withContext Result.failure(Exception("Recipient developer ID is required."))
            }

            if (senderId == receiverId) {
                return@withContext Result.failure(Exception("You cannot invite yourself to collaborate on your own project."))
            }

            // Pre-check if a pending or accepted invitation already exists
            val existingRequests = try {
                supabase.from("collaboration_requests")
                    .select {
                        filter {
                            eq("project_id", projectId)
                            eq("sender_id", senderId)
                            eq("receiver_id", receiverId)
                        }
                    }
                    .decodeList<CollaborationRequestDto>()
            } catch (_: Exception) {
                emptyList()
            }

            val pending = existingRequests.firstOrNull { it.status.equals("PENDING", ignoreCase = true) }
            if (pending != null) {
                return@withContext Result.failure(
                    Exception("A collaboration invitation is already pending for this developer on this project.")
                )
            }

            val accepted = existingRequests.firstOrNull { it.status.equals("ACCEPTED", ignoreCase = true) }
            if (accepted != null) {
                return@withContext Result.failure(
                    Exception("This developer is already a member of this project.")
                )
            }

            val requestPayload = buildJsonObject {
                put("project_id", projectId)
                put("sender_id", senderId)
                put("receiver_id", receiverId)
                put("proposed_role", "CONTRIBUTOR")
                put("message", message)
                put("status", "PENDING")
            }

            val createdDto = supabase.from("collaboration_requests")
                .insert(requestPayload) {
                    select()
                }
                .decodeSingle<CollaborationRequestDto>()

            val enrichedList = enrichRequests(listOf(createdDto))
            Result.success(enrichedList.firstOrNull() ?: createdDto.toDomain())
        } catch (e: Exception) {
            val friendlyMsg = e.toUserFriendlyMessage("Failed to send invitation. Please try again.")
            Result.failure(Exception(friendlyMsg))
        }
    }

    override suspend fun getReceivedRequests(): Result<List<CollaborationRequest>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val dtos = supabase.from("collaboration_requests")
                .select {
                    filter {
                        eq("receiver_id", userId)
                    }
                }
                .decodeList<CollaborationRequestDto>()

            val enriched = enrichRequests(dtos)
            Result.success(enriched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSentRequests(): Result<List<CollaborationRequest>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val dtos = supabase.from("collaboration_requests")
                .select {
                    filter {
                        eq("sender_id", userId)
                    }
                }
                .decodeList<CollaborationRequestDto>()

            val enriched = enrichRequests(dtos)
            Result.success(enriched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Try official Supabase SECURITY DEFINER RPC: respond_to_collaboration(req_id, response_status)
        try {
            supabase.postgrest.rpc(
                "respond_to_collaboration",
                buildJsonObject {
                    put("req_id", requestId)
                    put("response_status", "ACCEPTED")
                }
            )
            return@withContext Result.success(Unit)
        } catch (rpcEx1: Exception) {
            // Try lowercase if database enum requires "accepted"
            try {
                supabase.postgrest.rpc(
                    "respond_to_collaboration",
                    buildJsonObject {
                        put("req_id", requestId)
                        put("response_status", "accepted")
                    }
                )
                return@withContext Result.success(Unit)
            } catch (_: Exception) {}

            // 2. Direct table fallback if RPC fails or is restricted
            try {
                supabase.from("collaboration_requests")
                    .update(buildJsonObject { put("status", "ACCEPTED") }) {
                        filter { eq("id", requestId) }
                    }

                val req = supabase.from("collaboration_requests")
                    .select { filter { eq("id", requestId) } }
                    .decodeSingleOrNull<CollaborationRequestDto>()

                if (req != null) {
                    val project = supabase.from("projects")
                        .select { filter { eq("id", req.projectId) } }
                        .decodeSingleOrNull<ProjectDto>()

                    val newMemberUserId = if (project != null && req.senderId == project.ownerId) {
                        req.receiverId
                    } else {
                        req.senderId
                    }

                    val roleStr = req.proposedRole?.ifBlank { "CONTRIBUTOR" }?.lowercase() ?: "contributor"

                    try {
                        supabase.from("project_members")
                            .insert(buildJsonObject {
                                put("project_id", req.projectId)
                                put("user_id", newMemberUserId)
                                put("role", roleStr)
                            })
                    } catch (_: Exception) {}
                }
                Result.success(Unit)
            } catch (fallbackErr: Exception) {
                val err = rpcEx1 ?: fallbackErr
                Result.failure(Exception(err.toUserFriendlyMessage("Failed to accept invitation.")))
            }
        }
    }

    override suspend fun rejectRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Try official Supabase RPC: respond_to_collaboration(req_id, response_status)
        try {
            supabase.postgrest.rpc(
                "respond_to_collaboration",
                buildJsonObject {
                    put("req_id", requestId)
                    put("response_status", "REJECTED")
                }
            )
            return@withContext Result.success(Unit)
        } catch (rpcEx1: Exception) {
            try {
                supabase.postgrest.rpc(
                    "respond_to_collaboration",
                    buildJsonObject {
                        put("req_id", requestId)
                        put("response_status", "rejected")
                    }
                )
                return@withContext Result.success(Unit)
            } catch (_: Exception) {}

            // 2. Direct table fallback
            try {
                supabase.from("collaboration_requests")
                    .update(buildJsonObject { put("status", "REJECTED") }) {
                        filter { eq("id", requestId) }
                    }
                Result.success(Unit)
            } catch (fallbackErr: Exception) {
                val err = rpcEx1 ?: fallbackErr
                Result.failure(Exception(err.toUserFriendlyMessage("Failed to reject invitation.")))
            }
        }
    }

    override suspend fun cancelRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc(
                "respond_to_collaboration",
                buildJsonObject {
                    put("req_id", requestId)
                    put("response_status", "CANCELLED")
                }
            )
            return@withContext Result.success(Unit)
        } catch (_: Exception) {
            try {
                supabase.from("collaboration_requests")
                    .update(buildJsonObject { put("status", "CANCELLED") }) {
                        filter { eq("id", requestId) }
                    }
                Result.success(Unit)
            } catch (fallbackErr: Exception) {
                Result.failure(Exception(fallbackErr.toUserFriendlyMessage("Failed to cancel invitation.")))
            }
        }
    }

    override fun observeCollaborationRequests(userId: String): Flow<Unit> = callbackFlow {
        val channelTopic = "requests_${userId.take(8)}_${UUID.randomUUID().toString().take(6)}"
        val channel = supabase.realtime.channel(channelTopic)
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "collaboration_requests"
        }

        try {
            channel.subscribe()
        } catch (ignored: Exception) {}

        val job = launch {
            flow.collect { trySend(Unit) }
        }

        awaitClose {
            job.cancel()
            launch {
                try {
                    supabase.realtime.removeChannel(channel)
                } catch (ignored: Exception) {}
            }
        }
    }

    private suspend fun enrichRequests(dtos: List<CollaborationRequestDto>): List<CollaborationRequest> {
        if (dtos.isEmpty()) return emptyList()

        val projectIds = dtos.map { it.projectId }.filter { it.isNotBlank() }.distinct()
        val userIds = (dtos.map { it.senderId } + dtos.map { it.receiverId }).filter { it.isNotBlank() }.distinct()

        val projectsMap = try {
            if (projectIds.isNotEmpty()) {
                supabase.from("projects")
                    .select { filter { isIn("id", projectIds) } }
                    .decodeList<ProjectDto>()
                    .associateBy { it.id }
            } else emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

        val usersMap = try {
            if (userIds.isNotEmpty()) {
                supabase.from("developer_profiles")
                    .select { filter { isIn("id", userIds) } }
                    .decodeList<UserDto>()
                    .associateBy { it.id }
            } else emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

        return dtos.map { dto ->
            val projectDto = dto.project ?: projectsMap[dto.projectId]
            val senderDto = dto.sender ?: usersMap[dto.senderId]
            val receiverDto = dto.receiver ?: usersMap[dto.receiverId]

            dto.toDomain().copy(
                project = projectDto?.toDomain(),
                sender = senderDto?.toDomain(),
                receiver = receiverDto?.toDomain()
            )
        }
    }
}

