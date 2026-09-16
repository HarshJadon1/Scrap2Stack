package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.CollaborationRequest
import com.scrap2stack.app.domain.repository.CollaborationRepository

class GetReceivedCollaborationRequestsUseCase(private val repository: CollaborationRepository) {
    suspend operator fun invoke(): Result<List<CollaborationRequest>> {
        return repository.getReceivedRequests()
    }
}
