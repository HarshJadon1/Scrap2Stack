package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDto(
    val id: String = "",
    @SerialName("owner_id")
    val ownerId: String = "",
    val name: String,
    val description: String,
    val problem: String? = null,
    val category: String? = null,
    val technologies: List<String> = emptyList(),
    @SerialName("required_skills")
    val requiredSkills: List<String> = emptyList(),
    val status: String,
    @SerialName("github_url")
    val githubUrl: String? = null,
    @SerialName("github_repo_owner")
    val githubRepoOwner: String? = null,
    @SerialName("github_repo_name")
    val githubRepoName: String? = null,
    @SerialName("github_connected")
    val githubConnected: Boolean = false,
    @SerialName("github_connected_at")
    val githubConnectedAt: String? = null,
    @SerialName("github_default_branch")
    val githubDefaultBranch: String? = null,
    @SerialName("team_size")
    val teamSize: Int = 0,
    val difficulty: String = "INTERMEDIATE",
    @SerialName("revival_score")
    val revivalScore: Int = 0,
    @SerialName("quality_score")
    val qualityScore: Int = 0,
    @SerialName("analysis_id")
    val analysisId: String? = null,
    val progress: Int = 0,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class ProjectRecommendationDto(
    val project: ProjectDto,
    @SerialName("match_percentage")
    val matchPercentage: Int,
    val why: String,
    @SerialName("required_skills")
    val requiredSkills: List<String>,
    @SerialName("revival_score")
    val revivalScore: Int,
    @SerialName("score_breakdown")
    val scoreBreakdown: Map<String, Int>
)

@Serializable
data class CreateProjectRequest(
    @SerialName("owner_id")
    val ownerId: String? = null,
    val name: String,
    val description: String,
    val status: String,
    val problem: String? = null,
    val category: String? = null,
    val technologies: List<String> = emptyList(),
    @SerialName("required_skills")
    val requiredSkills: List<String> = emptyList(),
    @SerialName("github_url")
    val githubUrl: String? = null,
    @SerialName("team_size")
    val teamSize: Int = 4,
    val difficulty: String = "INTERMEDIATE",
    @SerialName("revival_score")
    val revivalScore: Int = 75
)

@Serializable
data class ProjectPaginationResponse(
    val projects: List<ProjectDto>,
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int
)
