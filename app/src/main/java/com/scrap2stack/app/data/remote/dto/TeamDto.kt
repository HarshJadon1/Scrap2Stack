package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String = "",
    val name: String = "",
    @SerialName("owner_id")
    val ownerId: String = "",
    val description: String? = null,
    @SerialName("max_members")
    val maxMembers: Int = 4,
    val status: String = "ACTIVE",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class TeamMemberDto(
    val id: String = "",
    @SerialName("team_id")
    val teamId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val role: String = "CONTRIBUTOR",
    val status: String = "ACTIVE",
    @SerialName("joined_at")
    val joinedAt: String = "",
    val user: UserDto? = null
)
