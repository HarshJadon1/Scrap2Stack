package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.ProjectMember

interface TeamRepository {
    suspend fun getProjectMembers(projectId: String): Result<List<ProjectMember>>
}
