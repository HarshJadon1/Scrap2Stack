package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.repository.ProjectRepository

class GetProjectAnalysisUseCase(private val repository: ProjectRepository) {
    suspend operator fun invoke(projectId: String): Result<ProjectAnalysis?> {
        return repository.getProjectAnalysis(projectId)
    }
}
