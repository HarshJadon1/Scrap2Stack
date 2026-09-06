package com.scrap2stack.app.domain.model

data class Developer(
    val id: String,
    val name: String,
    val username: String,
    val bio: String,
    val profileImageUrl: String? = null,
    val skills: List<String>,
    val interests: List<String>,
    val experienceLevel: String,
    val githubUsername: String,
    val charms: Int,
    val matchScore: Int? = null,
    val matchReason: String? = null
)
