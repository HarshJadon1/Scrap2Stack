package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.ProjectMember
import com.scrap2stack.app.domain.repository.TeamRepository

class GetProjectMembersUseCase(private val repository: TeamRepository) {
    suspend operator fun invoke(projectId: String): Result<List<ProjectMember>> {
        return repository.getProjectMembers(projectId)
    }
}
