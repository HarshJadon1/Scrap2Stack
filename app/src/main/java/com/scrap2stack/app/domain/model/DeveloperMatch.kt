package com.scrap2stack.app.domain.model

data class DeveloperMatch(
    val developer: Developer,
    val score: Int,
    val scoreBreakdown: MatchScoreBreakdown,
    val matchedSkills: List<String>,
    val missingSkills: List<String>,
    val matchedTechnologies: List<String>,
    val explanation: String,
    val compatibilityLabel: String
)

data class MatchScoreBreakdown(
    val skillScore: Double,
    val technologyScore: Double,
    val experienceScore: Double,
    val interestScore: Double,
    val categoryScore: Double,
    val proficiencyScore: Double? = null,
    val githubScore: Double? = null,
    val availabilityScore: Double? = null
)
