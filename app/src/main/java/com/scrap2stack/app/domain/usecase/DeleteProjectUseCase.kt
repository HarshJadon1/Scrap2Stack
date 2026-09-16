package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.repository.ProjectRepository

class DeleteProjectUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.deleteProject(id)
    }
}
