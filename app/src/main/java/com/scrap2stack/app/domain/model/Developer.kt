package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Developer(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val email: String? = null,
    val bio: String = "",
    val profileImageUrl: String? = null,
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val githubUrl: String = "",
    val githubUsername: String = "",
    val linkedinUrl: String = "",
    val portfolioUrl: String = "",
    val charms: Int = 0,
    val matchScore: Int? = null,
    val matchReason: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val displayName: String
        get() = name.ifBlank { username.ifBlank { "Developer" } }

    val charmsTier: String
        get() = when {
            charms >= 1500 -> "Tier 3: Grandmaster Alchemist"
            charms >= 500 -> "Tier 2: Code Alchemist"
            else -> "Tier 1: Apprentice Reviver"
        }

    val initials: String
        get() = displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifBlank { "D" }
}

enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    companion object {
        fun fromString(value: String?): ExperienceLevel {
            if (value.isNullOrBlank()) return BEGINNER
            return try {
                valueOf(value.trim().uppercase())
            } catch (e: Exception) {
                when (value.trim().uppercase()) {
                    "SENIOR", "EXPERT", "LEAD" -> ADVANCED
                    "MID", "JUNIOR_PLUS" -> INTERMEDIATE
                    else -> BEGINNER
                }
            }
        }
    }
}
