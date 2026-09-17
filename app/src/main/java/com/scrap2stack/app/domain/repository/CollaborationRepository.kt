package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.CollaborationRequest
import kotlinx.coroutines.flow.Flow

interface CollaborationRepository {
    suspend fun sendCollaborationRequest(projectId: String, receiverId: String, message: String): Result<CollaborationRequest>
    suspend fun getReceivedRequests(): Result<List<CollaborationRequest>>
    suspend fun getSentRequests(): Result<List<CollaborationRequest>>
    suspend fun acceptRequest(requestId: String): Result<Unit>
    suspend fun rejectRequest(requestId: String): Result<Unit>
    suspend fun cancelRequest(requestId: String): Result<Unit>
    fun observeCollaborationRequests(userId: String): Flow<Unit>
}
