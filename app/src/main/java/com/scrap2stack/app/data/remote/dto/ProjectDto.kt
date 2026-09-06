package com.scrap2stack.app.data.remote.dto

data class ProjectDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String,
    val problem: String?,
    val category: String?,
    val technologies: List<String>,
    val requiredSkills: List<String>,
    val status: String,
    val githubUrl: String?,
    val teamSize: Int,
    val difficulty: String,
    val revivalScore: Int,
    val qualityScore: Int,
    val analysisId: String?,
    val progress: Int,
    val createdAt: String,
    val updatedAt: String
)

data class ProjectRecommendationDto(
    val project: ProjectDto,
    val matchPercentage: Int,
    val why: String,
    val requiredSkills: List<String>,
    val revivalScore: Int,
    val scoreBreakdown: Map<String, Int>
)

data class CreateProjectRequest(
    val name: String,
    val description: String,
    val status: String,
    val problem: String? = null,
    val category: String? = null,
    val technologies: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val githubUrl: String? = null,
    val teamSize: Int = 4,
    val difficulty: String = "INTERMEDIATE"
)

data class ProjectPaginationResponse(
    val projects: List<ProjectDto>,
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int
)
