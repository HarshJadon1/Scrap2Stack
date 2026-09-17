package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CharmContribution(
    val id: String,
    val userId: String,
    val projectId: String,
    val contributionType: ContributionType,
    val charms: Int,
    val description: String,
    val referenceId: String?,
    val createdAt: String,
    val projectName: String? = null
)

enum class ContributionType {
    TASK_COMPLETED,
    TASK_HELPED,
    PROJECT_CONTRIBUTION,
    PROJECT_REVIVED,
    PROJECT_SHIPPED
}
