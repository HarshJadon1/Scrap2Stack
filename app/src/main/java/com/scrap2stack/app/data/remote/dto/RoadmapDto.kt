package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoadmapResponse(
    val roadmap: RoadmapDto? = null,
    val items: List<RoadmapItemDto> = emptyList()
)

@Serializable
data class RoadmapDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String = "",
    @SerialName("workspace_id")
    val workspaceId: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "ACTIVE",
    @SerialName("generated_by_ai")
    val generatedByAI: Boolean = true,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

@Serializable
data class RoadmapItemDto(
    val id: String = "",
    @SerialName("roadmap_id")
    val roadmapId: String = "",
    @SerialName("project_id")
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val status: String = "PLANNED",
    @SerialName("required_skills")
    val requiredSkills: List<String> = emptyList(),
    @SerialName("required_roles")
    val requiredRoles: List<String> = emptyList(),
    @SerialName("estimated_effort")
    val estimatedEffort: String = "",
    val dependencies: List<String> = emptyList(),
    val milestone: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)
