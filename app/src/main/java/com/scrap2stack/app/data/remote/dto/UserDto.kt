package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String = "",
    val username: String = "",
    val email: String? = null,
    @SerialName("profile_image")
    val profileImage: String? = null,
    val bio: String? = null,
    @SerialName("experience_level")
    val experienceLevel: String? = null,
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    @SerialName("github_url")
    val githubUrl: String? = null,
    @SerialName("linkedin_url")
    val linkedinUrl: String? = null,
    @SerialName("portfolio_url")
    val portfolioUrl: String? = null,
    val charms: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class ProfileUpdateRequest(
    val name: String,
    val username: String,
    val bio: String? = null,
    @SerialName("profile_image")
    val profileImage: String? = null,
    @SerialName("experience_level")
    val experienceLevel: String? = null,
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    @SerialName("github_url")
    val githubUrl: String? = null,
    @SerialName("linkedin_url")
    val linkedinUrl: String? = null,
    @SerialName("portfolio_url")
    val portfolioUrl: String? = null
)

@Serializable
data class ProfileUpsertRequest(
    val id: String,
    val name: String,
    val username: String,
    val bio: String? = null,
    @SerialName("profile_image")
    val profileImage: String? = null,
    @SerialName("experience_level")
    val experienceLevel: String? = null,
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    @SerialName("github_url")
    val githubUrl: String? = null,
    @SerialName("linkedin_url")
    val linkedinUrl: String? = null,
    @SerialName("portfolio_url")
    val portfolioUrl: String? = null
)
