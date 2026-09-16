package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedProjectDto(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("project_id")
    val projectId: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    val projects: ProjectDto? = null
)

@Serializable
data class SaveProjectRequest(
    @SerialName("user_id")
    val userId: String,
    @SerialName("project_id")
    val projectId: String
)
