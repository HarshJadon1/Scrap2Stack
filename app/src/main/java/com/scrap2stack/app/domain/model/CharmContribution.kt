package com.scrap2stack.app.domain.model

data class CharmContribution(
    val id: String,
    val userId: String,
    val projectId: String?,
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

object CharmsRules {
    const val TASK_COMPLETED = 10
    const val TASK_HELPED = 5
    const val PROJECT_CONTRIBUTION = 15
    const val PROJECT_REVIVED = 50
    const val PROJECT_SHIPPED = 100
}
