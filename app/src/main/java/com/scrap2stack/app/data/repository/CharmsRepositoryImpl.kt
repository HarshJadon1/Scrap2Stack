package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.CharmContributionDto
import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.domain.model.ContributionType
import com.scrap2stack.app.domain.repository.CharmsRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CharmsRepositoryImpl : CharmsRepository {

    override suspend fun getUserCharms(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val contributions = supabase.from("user_charms")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<CharmContributionDto>()

            Result.success(contributions.sumOf { it.charms })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getContributionHistory(): Result<List<CharmContribution>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val dtos = supabase.from("user_charms")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CharmContributionDto>()

            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun awardTaskCompletionCharms(taskId: String, projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Secure server-side awarding via RPC
            supabase.postgrest.rpc(
                "award_task_completion_charms",
                buildJsonObject {
                    put("p_task_id", taskId)
                    put("p_project_id", projectId)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun awardProjectRevivedCharms(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc(
                "award_project_revived_charms",
                buildJsonObject {
                    put("p_project_id", projectId)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun awardProjectShippedCharms(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.rpc(
                "award_project_shipped_charms",
                buildJsonObject {
                    put("p_project_id", projectId)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CharmContributionDto.toDomain(): CharmContribution {
        return CharmContribution(
            id = id,
            userId = userId,
            projectId = projectId,
            contributionType = try {
                ContributionType.valueOf(contributionType.uppercase())
            } catch (e: Exception) {
                ContributionType.PROJECT_CONTRIBUTION
            },
            charms = charms,
            description = description,
            referenceId = referenceId,
            createdAt = createdAt,
            projectName = project?.name
        )
    }
}
