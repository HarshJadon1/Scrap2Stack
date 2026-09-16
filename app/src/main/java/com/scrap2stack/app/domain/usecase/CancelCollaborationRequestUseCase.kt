package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.repository.CollaborationRepository

class CancelCollaborationRequestUseCase(private val repository: CollaborationRepository) {
    suspend operator fun invoke(requestId: String): Result<Unit> {
        return repository.cancelRequest(requestId)
    }
}
