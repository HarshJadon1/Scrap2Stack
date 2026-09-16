package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.CharmContribution
import kotlinx.coroutines.flow.Flow

interface CharmsRepository {
    suspend fun getUserCharms(): Result<Int>
    suspend fun getContributionHistory(): Result<List<CharmContribution>>
    suspend fun awardTaskCompletionCharms(taskId: String, projectId: String): Result<Unit>
    suspend fun awardProjectRevivedCharms(projectId: String): Result<Unit>
    suspend fun awardProjectShippedCharms(projectId: String): Result<Unit>
}
