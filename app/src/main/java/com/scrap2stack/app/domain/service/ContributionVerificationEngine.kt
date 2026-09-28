package com.scrap2stack.app.domain.service

import com.scrap2stack.app.domain.model.Developer
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt

enum class AnomalyStatus {
    CLEAN,
    SUSPICIOUS_RAPID_BURST,
    EMPTY_COMMIT_SPAM,
    COLLUSION_RISK
}

data class VerifiedSkillMetric(
    val skill: String,
    val confidence: Double,
    val verifiedContributionsCount: Int,
    val isVerified: Boolean,
    val confidenceLevelLabel: String
)

/**
 * ContributionVerificationEngine
 * 
 * Data science engine evaluating code contribution quality, diff-weighted scoring,
 * Bayesian skill verification confidence, and anti-gaming anomaly detection.
 */
object ContributionVerificationEngine {

    /**
     * Calculates diff-weighted contribution charms based on code lines changed,
     * files touched, and contribution category.
     */
    fun calculateDiffWeightedCharms(
        linesAdded: Int,
        linesDeleted: Int,
        type: String = "COMMIT",
        filesChanged: Int = 1
    ): Int {
        val totalLines = (linesAdded + linesDeleted).coerceAtLeast(0)
        
        val baseScore = when (type.uppercase()) {
            "PULL_REQUEST" -> 20
            "CODE_REVIEW" -> 15
            "COMMIT" -> 8
            "ISSUE" -> 5
            else -> 6
        }

        // Logarithmic scaling for diff volume: diminishing returns for giant refactors
        val diffBonus = (3.5 * ln(1.0 + totalLines.toDouble())).roundToInt().coerceIn(0, 25)
        val filesBonus = (filesChanged * 2).coerceIn(0, 10)

        return (baseScore + diffBonus + filesBonus).coerceIn(5, 55)
    }

    /**
     * Evaluates whether a stream of contributions exhibits signs of artificial reputation farming.
     */
    fun detectAnomaly(
        recentContributionsCount: Int,
        timeWindowSeconds: Long,
        totalLinesChanged: Int
    ): AnomalyStatus {
        return when {
            totalLinesChanged <= 0 -> AnomalyStatus.EMPTY_COMMIT_SPAM
            recentContributionsCount >= 10 && timeWindowSeconds < 180 -> AnomalyStatus.SUSPICIOUS_RAPID_BURST
            recentContributionsCount >= 25 && timeWindowSeconds < 600 -> AnomalyStatus.COLLUSION_RISK
            else -> AnomalyStatus.CLEAN
        }
    }

    /**
     * Calculates the Bayesian Skill Confidence Index for a developer and a specific skill.
     * C = 0.25 + 0.70 * (1 - e^(-0.4 * N))
     * N = verified contributions (derived from charms, completed tasks, and domain overlap).
     */
    fun getSkillConfidence(developer: Developer, targetSkill: String): VerifiedSkillMetric {
        val normTarget = SkillTaxonomyEngine.normalize(targetSkill)

        // Count implicit and explicit verified signals
        val hasDirectSkill = developer.skills.any { s -> SkillTaxonomyEngine.isSkillMatch(normTarget, s) }
        
        // Estimate verified contribution units from charms & track record
        // e.g. 100 charms ~= 2 verified contributions, 500 charms ~= 6 verified contributions
        val charmsContributions = (developer.charms / 60).coerceAtLeast(0)
        val hasGithub = developer.githubUrl.isNotBlank() || developer.githubUsername.isNotBlank()
        
        val verifiedCount = when {
            !hasDirectSkill -> 0
            hasGithub && charmsContributions > 0 -> charmsContributions + 1
            hasGithub -> 1
            charmsContributions > 0 -> charmsContributions
            else -> 0
        }

        // Bayesian probability update
        val confidence = if (hasDirectSkill) {
            val bayesianUpdate = 0.25 + 0.70 * (1.0 - exp(-0.40 * verifiedCount.toDouble()))
            ((bayesianUpdate * 100.0).roundToInt()) / 100.0
        } else {
            0.10
        }

        val isVerified = confidence >= 0.60
        val label = when {
            confidence >= 0.85 -> "Verified Master"
            confidence >= 0.60 -> "Verified Contributor"
            hasDirectSkill -> "Self-Reported"
            else -> "Unverified"
        }

        return VerifiedSkillMetric(
            skill = targetSkill,
            confidence = confidence,
            verifiedContributionsCount = verifiedCount,
            isVerified = isVerified,
            confidenceLevelLabel = label
        )
    }
}
