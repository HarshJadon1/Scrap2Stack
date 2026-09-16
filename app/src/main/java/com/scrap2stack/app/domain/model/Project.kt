package com.scrap2stack.app.domain.model

data class Project(
    val id: String = "",
    val ownerId: String = "",
    val name: String,
    val description: String,
    val technologies: List<String>,
    val requiredSkills: List<String>,
    val status: ProjectStatus,
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
    val githubDefaultBranch: String? = null
)

enum class ProjectStatus {
    IDEA,
    INCOMPLETE,
    ABANDONED,
    PAUSED,
    MVP_INCOMPLETE,
    REVIVING,
    COMPLETED,
    INACTIVE
}
