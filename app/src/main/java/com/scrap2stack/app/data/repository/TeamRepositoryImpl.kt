package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.ProjectMemberDto
import com.scrap2stack.app.data.remote.dto.UserDto
import com.scrap2stack.app.domain.model.*
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

            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProjectMemberDto.toDomain(): ProjectMember {
        return ProjectMember(
            id = id,
            projectId = projectId,
            userId = userId,
            role = try { ProjectRole.valueOf(role.uppercase()) } catch (e: Exception) { ProjectRole.CONTRIBUTOR },
            joinedAt = joinedAt,
            user = profiles?.toDomain()
        )
    }

    private fun UserDto.toDomain(): Developer {
        return Developer(
            id = id,
            name = name,
            username = username,
            bio = bio ?: "",
            profileImageUrl = profileImage,
            skills = skills,
            interests = interests,
            experienceLevel = try {
                ExperienceLevel.valueOf(experienceLevel?.uppercase() ?: "BEGINNER")
            } catch (e: Exception) {
                ExperienceLevel.BEGINNER
            },
            githubUrl = githubUrl ?: "",
            linkedinUrl = linkedinUrl ?: "",
            portfolioUrl = portfolioUrl ?: "",
            charms = charms,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }
}
