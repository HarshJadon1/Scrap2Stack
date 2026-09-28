package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.local.cache.AppCache
import com.scrap2stack.app.data.mapper.*
import com.scrap2stack.app.data.remote.dto.ProjectMemberDto
import com.scrap2stack.app.domain.model.ProjectMember
import com.scrap2stack.app.domain.repository.TeamRepository
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeamRepositoryImpl : TeamRepository {

    override suspend fun getProjectMembers(projectId: String): Result<List<ProjectMember>> = withContext(Dispatchers.IO) {
        try {
            val dtos = supabase.postgrest["project_members"].select {
                filter {
                    eq("project_id", projectId)
                }
            }.decodeList<ProjectMemberDto>()

            val members = dtos.map { it.toDomain() }
            AppCache.saveMembers(projectId, members)
            Result.success(members)
        } catch (e: Exception) {
            val cached = AppCache.getMembers(projectId)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
}
