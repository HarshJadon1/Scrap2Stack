package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository

class GetMyProjectsUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(): Result<List<Project>> {
        return repository.getMyProjects()
    }
}
