package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository

class UpdateProjectUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(project: Project): Result<Project> {
        return repository.updateProject(project)
    }
}
