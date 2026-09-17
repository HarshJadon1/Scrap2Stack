package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
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
            Result.failure(Exception("Failed to send invitation: ${e.localizedMessage}"))
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
        try {
            // Update request status to ACCEPTED
            supabase.from("collaboration_requests")
                .update(buildJsonObject { put("status", "ACCEPTED") }) {
                    filter { eq("id", requestId) }
                }

            // Retrieve request to find project and member IDs
            val req = supabase.from("collaboration_requests")
                .select { filter { eq("id", requestId) } }
                .decodeSingleOrNull<CollaborationRequestDto>()

            if (req != null) {
                // Fetch project to identify owner
                val project = supabase.from("projects")
                    .select { filter { eq("id", req.projectId) } }
                    .decodeSingleOrNull<ProjectDto>()

                // Correctly determine the member to add to project_members:
                // If sender was project owner -> member is receiver.
                // If sender was developer -> member is sender.
                val newMemberUserId = if (project != null && req.senderId == project.ownerId) {
                    req.receiverId
                } else {
                    req.senderId
                }

                val roleStr = req.proposedRole.ifBlank { "CONTRIBUTOR" }.lowercase()

                supabase.from("project_members")
                    .upsert(buildJsonObject {
                        put("project_id", req.projectId)
                        put("user_id", newMemberUserId)
                        put("role", roleStr)
                    })
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Attempt RPC procedure as secondary option
            try {
                supabase.postgrest.rpc(
                    "accept_collaboration_request",
                    buildJsonObject {
                        put("p_request_id", requestId)
                    }
                )
                Result.success(Unit)
            } catch (rpcEx: Exception) {
                Result.failure(Exception("Failed to accept request: ${e.localizedMessage}"))
            }
        }
    }

    override suspend fun rejectRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("collaboration_requests")
                .update(buildJsonObject { put("status", "REJECTED") }) {
                    filter { eq("id", requestId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("collaboration_requests")
                .update(buildJsonObject { put("status", "CANCELLED") }) {
                    filter { eq("id", requestId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

    private fun CollaborationRequestDto.toDomain(): CollaborationRequest {
        return CollaborationRequest(
            id = id,
            projectId = projectId,
            senderId = senderId,
            receiverId = receiverId,
            proposedRole = proposedRole,
            message = message,
            status = try { CollaborationStatus.valueOf(status.uppercase()) } catch (e: Exception) { CollaborationStatus.PENDING },
            createdAt = createdAt,
            project = project?.toDomain(),
            sender = sender?.toDomain(),
            receiver = receiver?.toDomain()
        )
    }

    private fun ProjectDto.toDomain(): Project {
        return Project(
            id = id,
            ownerId = ownerId,
            name = name,
            description = description,
            technologies = technologies,
            requiredSkills = requiredSkills,
            status = try { ProjectStatus.valueOf(status.uppercase()) } catch (e: Exception) { ProjectStatus.INACTIVE },
            revivalScore = revivalScore,
            lastActivity = updatedAt,
            teamSize = teamSize,
            problem = problem ?: "",
            category = category ?: "",
            githubUrl = githubUrl ?: ""
        )
    }

    private fun UserDto.toDomain(): Developer {
        return Developer(
            id = id,
            name = name,
            username = username,
            bio = bio ?: "",
            profileImageUrl = profileImage,
            skills = skills,
            interests = interests,
            experienceLevel = try {
                ExperienceLevel.valueOf(experienceLevel?.uppercase() ?: "BEGINNER")
            } catch (e: Exception) {
                ExperienceLevel.BEGINNER
            },
            githubUrl = githubUrl ?: "",
            linkedinUrl = linkedinUrl ?: "",
            portfolioUrl = portfolioUrl ?: "",
            charms = charms,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }
}
