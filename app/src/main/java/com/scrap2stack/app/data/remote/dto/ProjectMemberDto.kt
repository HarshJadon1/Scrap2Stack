package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectMemberDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String,
    @SerialName("user_id")
    val userId: String,
    val role: String,
    @SerialName("joined_at")
    val joinedAt: String = "",
    val profiles: UserDto? = null
)
