package com.scrap2stack.app.data.remote.dto

data class TaskDto(
    val id: String,
    val projectId: String,
    val workspaceId: String,
    val title: String,
    val description: String?,
    val assigneeId: String?,
    val createdBy: String,
    val skill: String,
    val role: String,
    val priority: String,
    val status: String,
    val dueDate: String?,
    val estimatedHours: Int,
    val actualHours: Int,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?
)

data class CreateTaskRequest(
    val title: String,
    val description: String?,
    val skill: String,
    val role: String,
    val priority: String,
    val assigneeId: String? = null,
    val dueDate: String? = null,
    val estimatedHours: Int = 0
)

data class UpdateTaskStatusRequest(
    val status: String
)
