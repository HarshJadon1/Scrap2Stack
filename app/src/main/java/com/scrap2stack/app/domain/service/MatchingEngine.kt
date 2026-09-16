package com.scrap2stack.app.domain.service

import com.scrap2stack.app.domain.model.*
import java.util.Locale

class MatchingEngine {

    fun calculateMatch(project: Project, developer: Developer): DeveloperMatch {
        val matchedSkills = project.requiredSkills.filter { req ->
            developer.skills.any { dev -> dev.equals(req, ignoreCase = true) }
        }
        val missingSkills = project.requiredSkills.filterNot { req ->
            developer.skills.any { dev -> dev.equals(req, ignoreCase = true) }
        }

        val matchedTechnologies = project.technologies.filter { tech ->
            developer.skills.any { dev -> dev.equals(tech, ignoreCase = true) } ||
            developer.interests.any { dev -> dev.equals(tech, ignoreCase = true) }
        }

        // Scoring
        var skillScore = 0.0
        if (project.requiredSkills.isNotEmpty()) {
            skillScore = (matchedSkills.size.toDouble() / project.requiredSkills.size.toDouble()) * 100
        }

        var technologyScore = 0.0
        if (project.technologies.isNotEmpty()) {
            technologyScore = (matchedTechnologies.size.toDouble() / project.technologies.size.toDouble()) * 100
        }

        val experienceScore = calculateExperienceScore(project, developer)
        val interestScore = calculateInterestScore(project, developer)
        
        // Category score - using description/problem for now if category is missing
        val categoryScore = calculateCategoryScore(project, developer)

        // Weight normalization (excluding unavailable signals: Proficiency, GitHub, Availability)
        // Original weights: Skills 40%, Proficiency 15%, Tech 15%, Exp 10%, Interests 5%, Cat 5%, GitHub 5%, Avail 5%
        // Available: Skills, Tech, Exp, Interests, Cat
        // Sum of available weights = 40 + 15 + 10 + 5 + 5 = 75
        
        val totalWeightedScore = (skillScore * 0.40) + 
                                (technologyScore * 0.15) + 
                                (experienceScore * 0.10) + 
                                (interestScore * 0.05) + 
                                (categoryScore * 0.05)
        
        val normalizedScore = (totalWeightedScore / 0.75).toInt().coerceIn(0, 100)

        val explanation = generateExplanation(matchedSkills, matchedTechnologies, developer, normalizedScore)
        val label = getCompatibilityLabel(normalizedScore)

        return DeveloperMatch(
            developer = developer,
            score = normalizedScore,
            scoreBreakdown = MatchScoreBreakdown(
                skillScore = skillScore,
                technologyScore = technologyScore,
                experienceScore = experienceScore,
                interestScore = interestScore,
                categoryScore = categoryScore
            ),
            matchedSkills = matchedSkills,
            missingSkills = missingSkills,
            matchedTechnologies = matchedTechnologies,
            explanation = explanation,
            compatibilityLabel = label
        )
    }

    private fun calculateExperienceScore(project: Project, developer: Developer): Double {
        // Simple logic: If project is complex (vaguely determined by number of reqs), prefer ADVANCED
        val complexity = if (project.requiredSkills.size > 5) ExperienceLevel.ADVANCED else ExperienceLevel.INTERMEDIATE
        
        return when (developer.experienceLevel) {
            ExperienceLevel.ADVANCED -> if (complexity == ExperienceLevel.ADVANCED) 100.0 else 90.0
            ExperienceLevel.INTERMEDIATE -> if (complexity == ExperienceLevel.ADVANCED) 70.0 else 100.0
            ExperienceLevel.BEGINNER -> if (complexity == ExperienceLevel.ADVANCED) 40.0 else 70.0
        }
    }

    private fun calculateInterestScore(project: Project, developer: Developer): Double {
        val keywords = (project.name + " " + project.description).lowercase(Locale.ROOT)
        val matchedInterests = developer.interests.filter { interest ->
            keywords.contains(interest.lowercase(Locale.ROOT))
        }
        if (developer.interests.isEmpty()) return 0.0
        return (matchedInterests.size.toDouble() / developer.interests.size.toDouble()) * 100
    }

    private fun calculateCategoryScore(project: Project, developer: Developer): Double {
        // Placeholder as Category is not explicitly in models yet. 
        // We'll treat it as a secondary interest match.
        return calculateInterestScore(project, developer)
    }

    private fun getCompatibilityLabel(score: Int): String {
        return when {
            score >= 92 -> "Excellent Match"
            score >= 80 -> "Strong Match"
            score >= 65 -> "Good Match"
            score >= 50 -> "Potential Match"
            else -> "Low Match"
        }
    }

    private fun generateExplanation(
        matchedSkills: List<String>,
        matchedTech: List<String>,
        developer: Developer,
        score: Int
    ): String {
        val skillPart = if (matchedSkills.isNotEmpty()) {
            "the developer has ${matchedSkills.take(3).joinToString(", ")} skills"
        } else "technical background"

        val techPart = if (matchedTech.isNotEmpty()) {
            "matches the project stack (${matchedTech.take(2).joinToString(", ")})"
        } else "vague technical alignment"

        val base = when {
            score >= 80 -> "Strong match because $skillPart and $techPart."
            score >= 50 -> "Good alignment in $skillPart."
            else -> "Some technical overlap detected."
        }

        return "$base GitHub contribution and availability data is not yet available."
    }
}
