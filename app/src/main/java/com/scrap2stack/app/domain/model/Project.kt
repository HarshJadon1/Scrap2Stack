package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Project(
    val id: String = "",
    val ownerId: String = "",
    val name: String,
    val description: String,
    val technologies: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val status: ProjectStatus = ProjectStatus.INCOMPLETE,
    val revivalScore: Int = 0,
    val qualityScore: Int = 0,
    val progress: Int = 0,
    val lastActivity: String = "",
    val teamSize: Int = 4,
    val problem: String = "",
    val category: String = "",
    val githubUrl: String = "",
    val githubRepoOwner: String? = null,
    val githubRepoName: String? = null,
    val githubConnected: Boolean = false,
    val githubConnectedAt: String? = null,
    val githubDefaultBranch: String? = null,
    val owner: Developer? = null,
    val members: List<ProjectMember> = emptyList(),
    val tasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val isCompleted: Boolean
        get() = status == ProjectStatus.COMPLETED

    val isReviving: Boolean
        get() = status == ProjectStatus.REVIVING

    val isAbandoned: Boolean
        get() = status == ProjectStatus.ABANDONED

    val revivalTier: String
        get() = when {
            revivalScore >= 90 -> "Tier S (Exceptional)"
            revivalScore >= 75 -> "Tier A (High Potential)"
            revivalScore >= 60 -> "Tier B (Viable)"
            else -> "Tier C (Needs Work)"
        }
}

enum class ProjectStatus {
    IDEA,
    INCOMPLETE,
    ABANDONED,
    PAUSED,
    MVP_INCOMPLETE,
    REVIVING,
    COMPLETED,
    INACTIVE;

    companion object {
        fun fromString(value: String?): ProjectStatus {
            if (value.isNullOrBlank()) return INCOMPLETE
            return try {
                valueOf(value.trim().uppercase())
            } catch (e: Exception) {
                when (value.trim().uppercase()) {
                    "IN_PROGRESS", "ACTIVE" -> REVIVING
                    "DONE" -> COMPLETED
                    else -> INCOMPLETE
                }
            }
        }
    }
}
