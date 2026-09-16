package com.scrap2stack.app.domain.model

data class ProjectAnalysis(
    val id: String,
    val projectId: String,
    val projectSummary: String,
    val currentState: String,
    val missingComponents: List<MissingComponent>,
    val requiredSkills: List<RequiredSkillRecommendation>,
    val roadmap: List<RoadmapStep>,
    val revivalScore: Int,
    val scoreExplanation: String,
    val risks: List<ProjectRisk>,
    val recommendations: List<String>,
    val analyzedAt: String,
    val isStale: Boolean = false
)

data class MissingComponent(
    val title: String,
    val description: String,
    val priority: Priority
)

data class RequiredSkillRecommendation(
    val skill: String,
    val importance: Importance
)

data class RoadmapStep(
    val step: Int,
    val title: String,
    val description: String,
    val priority: Priority
)

data class ProjectRisk(
    val risk: String,
    val severity: Priority,
    val explanation: String
)

enum class Priority {
    HIGH, MEDIUM, LOW
}

enum class Importance {
    REQUIRED, RECOMMENDED, OPTIONAL
}
