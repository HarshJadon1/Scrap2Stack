package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("status")
    val status: String = "TODO",
    @SerialName("priority")
    val priority: String = "MEDIUM",
    @SerialName("assignee_id")
    val assigneeId: String? = null,
    @SerialName("skill")
    val skill: String = "",
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("roadmap_step_id")
    val roadmapStepId: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("completed_at")
    val completedAt: String? = null
)

@Serializable
data class CreateTaskRequest(
    @SerialName("project_id")
    val projectId: String,
    val title: String,
    val description: String? = null,
    val priority: String = "MEDIUM",
    @SerialName("assignee_id")
    val assigneeId: String? = null,
    @SerialName("roadmap_step_id")
    val roadmapStepId: String? = null
)

@Serializable
data class UpdateTaskStatusRequest(
    val status: String
)
