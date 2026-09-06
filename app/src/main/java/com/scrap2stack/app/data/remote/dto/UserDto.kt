package com.scrap2stack.app.data.remote.dto

data class UserDto(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val profileImage: String?,
    val bio: String?,
    val experienceLevel: String?,
    val skills: List<String>,
    val interests: List<String>,
    val githubUsername: String?,
    val githubProfileUrl: String?,
    val charms: Int,
    val createdAt: String,
    val updatedAt: String
)
