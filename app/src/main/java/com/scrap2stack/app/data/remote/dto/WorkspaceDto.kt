package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkspaceDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String = "",
    @SerialName("team_id")
    val teamId: String = "",
    val name: String = "",
    val description: String? = null,
    val status: String = "ACTIVE",
    val progress: Double = 0.0,
    @SerialName("current_phase")
    val currentPhase: String = "Phase 1",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)
