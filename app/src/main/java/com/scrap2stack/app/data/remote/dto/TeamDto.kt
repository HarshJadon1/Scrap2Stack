package com.scrap2stack.app.data.remote.dto

data class TeamDto(
    val id: String,
    val projectId: String,
    val name: String,
    val ownerId: String,
    val description: String?,
    val maxMembers: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

data class TeamMemberDto(
    val id: String,
    val teamId: String,
    val userId: String,
    val role: String,
    val status: String,
    val joinedAt: String,
    val user: UserDto? = null // Optionally populated
)
