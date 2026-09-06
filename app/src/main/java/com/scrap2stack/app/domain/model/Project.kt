package com.scrap2stack.app.domain.model

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val technologies: List<String>,
    val requiredSkills: List<String>,
    val status: ProjectStatus,
    val revivalScore: Int,
    val lastActivity: String,
    val teamSize: Int,
    val problem: String = "",
    val githubUrl: String = ""
)

enum class ProjectStatus {
    ABANDONED,
    REVIVING,
    COMPLETED,
    INACTIVE
}
