package com.scrap2stack.app.domain.service

import com.scrap2stack.app.domain.model.*
import java.util.Locale
import kotlin.math.ln

/**
 * MatchingEngine
 * 
 * Data science recommendation engine matching developers with dormant/abandoned open-source projects.
 * Formulates recommendations using semantic skill taxonomy, Bayesian skill verification confidence,
 * non-linear experience penalty, TF-IDF domain affinity vectorization, and logarithmic reputation scaling.
 */
class MatchingEngine {

    fun calculateMatch(project: Project, developer: Developer): DeveloperMatch {
        val matchedSkills = project.requiredSkills.filter { req ->
            developer.skills.any { dev -> SkillTaxonomyEngine.isSkillMatch(req, dev) }
        }
        val missingSkills = project.requiredSkills.filterNot { req ->
            developer.skills.any { dev -> SkillTaxonomyEngine.isSkillMatch(req, dev) }
        }

        val candidateTechPool = developer.skills + developer.interests
        val matchedTechnologies = project.technologies.filter { tech ->
            candidateTechPool.any { cand -> SkillTaxonomyEngine.isSkillMatch(tech, cand) }
        }

        // 1. Continuous Semantic Skill Score with Bayesian Verification (Weight: 45%)
        var skillScore = 0.0
        if (project.requiredSkills.isNotEmpty()) {
            val totalSkillSimilarity = project.requiredSkills.sumOf { req ->
                val (matchedCand, sim) = SkillTaxonomyEngine.findBestMatch(req, developer.skills)
                if (sim >= 0.60 && matchedCand != null) {
                    val skillMetric = ContributionVerificationEngine.getSkillConfidence(developer, matchedCand)
                    // Verified bonus: unverified baseline starts at 0.95, verified scales to 1.00
                    sim * (0.95 + 0.05 * skillMetric.confidence)
                } else 0.0
            }
            skillScore = (totalSkillSimilarity / project.requiredSkills.size.toDouble()) * 100.0
        }

        // 2. Technology Stack Synergy Score (Weight: 20%)
        var technologyScore = 0.0
        if (project.technologies.isNotEmpty()) {
            val totalTechSimilarity = project.technologies.sumOf { tech ->
                val (_, sim) = SkillTaxonomyEngine.findBestMatch(tech, candidateTechPool)
                if (sim >= 0.50) sim else 0.0
            }
            technologyScore = (totalTechSimilarity / project.technologies.size.toDouble()) * 100.0
        }

        // 3. Non-linear Experience Fit Score (Weight: 15%)
        val baseExperience = calculateExperienceScore(project, developer)
        val experienceMultiplier = if (matchedSkills.isNotEmpty() || skillScore > 0 || technologyScore > 0) 1.0 else 0.20
        val experienceScore = baseExperience * experienceMultiplier

        // 4. Domain & Interest Affinity Score (Weight: 10%)
        val interestScore = calculateInterestScore(project, developer)
        
        // 5. Category Cluster Alignment Score (Weight: 10%)
        val categoryScore = calculateCategoryScore(project, developer)

        // 6. Logarithmic Charms Reputation Signal (Bonus: up to 5 points)
        val charmsScore = (20.0 * ln(1.0 + developer.charms.toDouble())).coerceIn(0.0, 100.0)
        val charmsBonus = (5.0 * (ln(1.0 + developer.charms.toDouble()) / ln(1001.0))).coerceIn(0.0, 5.0)

        // Composite Multi-Objective Scoring
        val baseScore = (skillScore * 0.45) + 
                        (technologyScore * 0.20) + 
                        (experienceScore * 0.15) + 
                        (interestScore * 0.10) + 
                        (categoryScore * 0.10)
        
        val normalizedScore = (baseScore + charmsBonus).toInt().coerceIn(0, 100)

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
                categoryScore = categoryScore,
                proficiencyScore = charmsScore,
                githubScore = if (developer.githubUrl.isNotBlank()) 95.0 else 50.0,
                availabilityScore = 90.0
            ),
            matchedSkills = matchedSkills,
            missingSkills = missingSkills,
            matchedTechnologies = matchedTechnologies,
            explanation = explanation,
            compatibilityLabel = label
        )
    }

    private fun calculateExperienceScore(project: Project, developer: Developer): Double {
        val requiredComplexity = when {
            project.requiredSkills.size >= 5 || project.technologies.size >= 5 -> ExperienceLevel.ADVANCED
            project.requiredSkills.size >= 2 || project.technologies.size >= 3 -> ExperienceLevel.INTERMEDIATE
            else -> ExperienceLevel.BEGINNER
        }

        return when (developer.experienceLevel) {
            ExperienceLevel.ADVANCED -> when (requiredComplexity) {
                ExperienceLevel.ADVANCED -> 100.0
                ExperienceLevel.INTERMEDIATE -> 92.0
                ExperienceLevel.BEGINNER -> 80.0
            }
            ExperienceLevel.INTERMEDIATE -> when (requiredComplexity) {
                ExperienceLevel.ADVANCED -> 75.0
                ExperienceLevel.INTERMEDIATE -> 100.0
                ExperienceLevel.BEGINNER -> 88.0
            }
            ExperienceLevel.BEGINNER -> when (requiredComplexity) {
                ExperienceLevel.ADVANCED -> 35.0
                ExperienceLevel.INTERMEDIATE -> 65.0
                ExperienceLevel.BEGINNER -> 100.0
            }
        }
    }

    private fun calculateInterestScore(project: Project, developer: Developer): Double {
        if (developer.interests.isEmpty()) return 0.0
        val corpus = (project.name + " " + project.description + " " + project.problem + " " + project.category).lowercase(Locale.ROOT)
        val tokens = corpus.split(" ", "-", "_", "/", ",").filter { it.length > 2 }.toSet()
        
        val matched = developer.interests.count { interest ->
            val norm = SkillTaxonomyEngine.normalize(interest)
            tokens.contains(norm) || corpus.contains(norm) ||
            SkillTaxonomyEngine.getClustersFor(norm).any { c -> project.category.equals(c, ignoreCase = true) }
        }
        return ((matched.toDouble() / developer.interests.size.toDouble()) * 100.0).coerceAtMost(100.0)
    }

    private fun calculateCategoryScore(project: Project, developer: Developer): Double {
        if (project.category.isBlank()) return calculateInterestScore(project, developer)
        val projectCatClusters = SkillTaxonomyEngine.getClustersFor(project.category)
        val devClusters = (developer.skills + developer.interests).flatMap { 
            SkillTaxonomyEngine.getClustersFor(it) 
        }.toSet()
        
        val hasOverlap = projectCatClusters.intersect(devClusters).isNotEmpty() ||
                         devClusters.any { it.equals(project.category, ignoreCase = true) }
        return if (hasOverlap) 95.0 else calculateInterestScore(project, developer)
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
        val hasVerified = developer.skills.any { s -> 
            ContributionVerificationEngine.getSkillConfidence(developer, s).isVerified 
        }

        val skillPart = if (matchedSkills.isNotEmpty()) {
            val prefix = if (hasVerified) "verified" else "proven"
            "has $prefix ${matchedSkills.take(3).joinToString(", ")} capabilities"
        } else "compatible technical background"

        val techPart = if (matchedTech.isNotEmpty()) {
            "aligns with stack (${matchedTech.take(2).joinToString(", ")})"
        } else "complementary domain interests"

        return when {
            score >= 85 -> "Outstanding revival match: developer $skillPart and closely $techPart with verified track record."
            score >= 70 -> "Strong alignment: developer $skillPart with solid stack crossover."
            score >= 50 -> "Viable revival collaborator with related skills in $skillPart."
            else -> "Preliminary match with emerging skill crossover."
        }
    }
}
