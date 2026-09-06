package com.scrap2stack.app.data.remote.dto

data class AnalysisDto(
    val id: String,
    val projectId: String,
    val status: String,
    val errorCode: String?,
    val summary: String,
    val problemStatement: String,
    val projectState: String,
    val detectedTechnologies: List<TechnologyMatchDto>,
    val requiredSkills: List<SkillRequirementDto>,
    val missingSkills: List<String>,
    val qualityScore: Int,
    val qualityFactors: List<ScoreFactorDto>,
    val revivalScore: Int,
    val revivalRecommendation: String,
    val explanation: String,
    val complexity: String,
    val estimatedEffort: String,
    val risks: List<RiskFactorDto>,
    val recommendedTeamSize: Int,
    val recommendedRoles: List<String>,
    val roadmap: List<RoadmapStepDto>,
    val nextSteps: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class TechnologyMatchDto(
    val name: String,
    val confidence: Double
)

data class SkillRequirementDto(
    val name: String,
    val confidence: Double,
    val importance: String,
    val why: String
)

data class RiskFactorDto(
    val risk: String,
    val severity: String,
    val explanation: String
)

data class ScoreFactorDto(
    val name: String,
    val score: Int,
    val explanation: String? = null
)

data class RoadmapStepDto(
    val phase: Int,
    val title: String,
    val description: String,
    val requiredSkills: List<String>,
    val estimatedEffort: String,
    val priority: String? = null
)
