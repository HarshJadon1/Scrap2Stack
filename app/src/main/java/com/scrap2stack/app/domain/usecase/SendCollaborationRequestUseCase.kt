package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.CollaborationRequest
import com.scrap2stack.app.domain.repository.CollaborationRepository

class SendCollaborationRequestUseCase(private val repository: CollaborationRepository) {
    suspend operator fun invoke(projectId: String, receiverId: String, message: String): Result<CollaborationRequest> {
        return repository.sendCollaborationRequest(projectId, receiverId, message)
    }
}
