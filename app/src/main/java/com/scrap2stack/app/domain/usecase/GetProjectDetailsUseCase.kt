package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository

class GetProjectDetailsUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(id: String): Result<Project> {
        return repository.getProjectById(id)
    }
}
