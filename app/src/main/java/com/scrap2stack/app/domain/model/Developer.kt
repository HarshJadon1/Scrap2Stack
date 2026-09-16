package com.scrap2stack.app.domain.model

data class Developer(
    val id: String,
    val name: String,
    val username: String,
    val bio: String,
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
)

enum class ExperienceLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}
