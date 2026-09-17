package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Task(
    val id: String,
    val title: String,
    val assignee: String?,
    val priority: TaskPriority,
    val skill: String,
    val status: TaskStatus,
    val dueDate: String? = null
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskStatus {
    TODO, IN_PROGRESS, REVIEW, COMPLETED
}
