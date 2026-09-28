package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

enum class RoadmapStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED;

    companion object {
        fun fromString(value: String): RoadmapStatus = when (value.trim().uppercase()) {
            "IN_PROGRESS", "PROGRESS", "DOING" -> IN_PROGRESS
            "COMPLETED", "DONE" -> COMPLETED
            else -> PLANNED
        }
    }
}

@Immutable
data class RoadmapItem(
    val id: String = "",
    val roadmapId: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val status: String = "PLANNED",
    val requiredSkills: List<String> = emptyList(),
    val requiredRoles: List<String> = emptyList(),
    val estimatedEffort: String = "",
    val dependencies: List<String> = emptyList(),
    val milestone: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    val roadmapStatus: RoadmapStatus
        get() = RoadmapStatus.fromString(status)

    val isCompleted: Boolean
        get() = roadmapStatus == RoadmapStatus.COMPLETED

    val isInProgress: Boolean
        get() = roadmapStatus == RoadmapStatus.IN_PROGRESS
}
