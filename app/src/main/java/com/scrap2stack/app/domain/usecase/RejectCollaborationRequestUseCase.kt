package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.repository.CollaborationRepository

class RejectCollaborationRequestUseCase(private val repository: CollaborationRepository) {
    suspend operator fun invoke(requestId: String): Result<Unit> {
        return repository.rejectRequest(requestId)
    }
}
