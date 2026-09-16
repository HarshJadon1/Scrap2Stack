package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.domain.repository.CollaborationRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CollaborationRepositoryImpl : CollaborationRepository {

    override suspend fun sendCollaborationRequest(
        projectId: String,
        receiverId: String,
        message: String
    ): Result<CollaborationRequest> = withContext(Dispatchers.IO) {
        try {
            val senderId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val request = CollaborationRequestDto(
                projectId = projectId,
                senderId = senderId,
                receiverId = receiverId,
                message = message
            )

            val createdDto = supabase.from("collaboration_requests")
                .insert(request) {
                    select()
                }
                .decodeSingle<CollaborationRequestDto>()

            Result.success(createdDto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
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

            Result.success(dtos.map { it.toDomain() })
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

            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Using Supabase RPC for atomic acceptance
            // The PostgreSQL function 'accept_collaboration_request' must be created
            supabase.postgrest.rpc(
                "accept_collaboration_request",
                buildJsonObject {
                    put("p_request_id", requestId)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

    private fun CollaborationRequestDto.toDomain(): CollaborationRequest {
        return CollaborationRequest(
            id = id,
            projectId = projectId,
            senderId = senderId,
            receiverId = receiverId,
            proposedRole = proposedRole,
            message = message,
            status = try { CollaborationStatus.valueOf(status.uppercase()) } catch (e: Exception) { CollaborationStatus.PENDING },
            createdAt = createdAt
        )
    }
}
