package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Task(
    val id: String = "",
    val projectId: String = "",
    val title: String,
    val description: String = "",
    val assignee: String? = null,
    val assigneeUser: Developer? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val skill: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val dueDate: String? = null,
    val completedAt: String? = null,
    val roadmapStepId: String? = null,
    val createdAt: String = ""
) {
    val isCompleted: Boolean
        get() = status == TaskStatus.COMPLETED

    val isInProgress: Boolean
        get() = status == TaskStatus.IN_PROGRESS

    val isUrgent: Boolean
        get() = priority == TaskPriority.URGENT

    val isInReview: Boolean
        get() = status == TaskStatus.REVIEW
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT;

    companion object {
        fun fromString(value: String?): TaskPriority {
            if (value.isNullOrBlank()) return MEDIUM
            return try {
                valueOf(value.trim().uppercase())
            } catch (e: Exception) {
                MEDIUM
            }
        }
    }
}

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    REVIEW,
    COMPLETED;

    companion object {
        fun fromString(value: String?): TaskStatus {
            if (value.isNullOrBlank()) return TODO
            return when (value.trim().uppercase()) {
                "IN_PROGRESS", "PROGRESS", "DOING" -> IN_PROGRESS
                "REVIEW", "IN_REVIEW" -> REVIEW
                "COMPLETED", "DONE" -> COMPLETED
                else -> TODO
            }
        }
    }
}
