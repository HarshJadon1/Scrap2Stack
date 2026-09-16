package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String,
    val summary: String,
    @SerialName("current_state")
    val currentState: String,
    @SerialName("missing_components")
    val missingComponents: List<MissingComponentDto>,
    @SerialName("required_skills")
    val requiredSkills: List<SkillRequirementDto>,
    @SerialName("missing_skills")
    val missingSkills: List<String> = emptyList(),
    val roadmap: List<RoadmapStepDto>,
    @SerialName("revival_score")
    val revivalScore: Int,
    @SerialName("quality_score")
    val qualityScore: Int,
    @SerialName("revival_recommendation")
    val revivalRecommendation: String,
    val explanation: String,
    val risks: List<RiskFactorDto>,
    val recommendations: List<String>,
    @SerialName("next_steps")
    val nextSteps: List<String> = emptyList(),
    val complexity: String,
    @SerialName("estimated_effort")
    val estimatedEffort: String,
    @SerialName("recommended_team_size")
    val recommendedTeamSize: Int,
    @SerialName("detected_technologies")
    val detectedTechnologies: List<TechnologyDto>,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class MissingComponentDto(
    val title: String,
    val description: String,
    val priority: String
)

@Serializable
data class SkillRequirementDto(
    val name: String,
    val level: String = "INTERMEDIATE",
    val importance: String = "MEDIUM",
    val confidence: Double,
    val why: String
)

@Serializable
data class RoadmapStepDto(
    val phase: Int,
    val title: String,
    val description: String,
    val priority: String? = null,
    @SerialName("estimated_effort")
    val estimatedEffort: String
)

@Serializable
data class RiskFactorDto(
    val risk: String,
    val severity: String,
    val explanation: String
)

@Serializable
data class TechnologyDto(
    val name: String,
    val confidence: Double
)
