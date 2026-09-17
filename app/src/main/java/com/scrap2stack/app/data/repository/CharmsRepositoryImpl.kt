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

/**
 * Implementation of [CharmsRepository] that interacts with Supabase for charm-related operations.
 *
 * This repository manages user charm balances, retrieves contribution history, and invokes
 * remote procedure calls (RPCs) to award charms for various user achievements (task completions,
 * project revivals, and project shipments).
 */
class CharmsRepositoryImpl : CharmsRepository {

    /**
     * Calculates the total number of charms accumulated by the currently authenticated user.
     *
     * Queries the `user_charms` table filtered by the current user's ID and sums all charm amounts.
     *
     * @return A [Result] containing the total charm count as an [Int] on success,
     * or a [Result.failure] if the user is unauthenticated or a network error occurs.
     */
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

    /**
     * Retrieves the charm contribution history for the currently authenticated user.
     *
     * Fetches entries from the `user_charms` table, ordered from newest to oldest (`created_at` DESC),
     * and maps each DTO to the domain model [CharmContribution].
     *
     * @return A [Result] containing a list of [CharmContribution] items on success,
     * or a [Result.failure] if the user is unauthenticated or a query error occurs.
     */
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

    /**
     * Triggers the remote procedure call (RPC) to award charms for completing a task.
     *
     * @param taskId The ID of the completed task.
     * @param projectId The ID of the project associated with the task.
     * @return A [Result] containing [Unit] on success, or a [Result.failure] if the RPC call fails.
     */
    override suspend fun awardTaskCompletionCharms(taskId: String, projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
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

    /**
     * Triggers the remote procedure call (RPC) to award charms for reviving an inactive project.
     *
     * @param projectId The ID of the revived project.
     * @return A [Result] containing [Unit] on success, or a [Result.failure] if the RPC call fails.
     */
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

    /**
     * Triggers the remote procedure call (RPC) to award charms for shipping a project.
     *
     * @param projectId The ID of the shipped project.
     * @return A [Result] containing [Unit] on success, or a [Result.failure] if the RPC call fails.
     */
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

    /**
     * Maps a [CharmContributionDto] remote data transfer object to its domain representation [CharmContribution].
     *
     * Handles safe parsing of [ContributionType] enum strings, falling back to
     * [ContributionType.PROJECT_CONTRIBUTION] if parsing fails.
     *
     * @return The mapped [CharmContribution] domain object.
     */
    private fun CharmContributionDto.toDomain(): CharmContribution {
        return CharmContribution(
            id = id,
            userId = userId,
            projectId = projectId ?: "",
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
