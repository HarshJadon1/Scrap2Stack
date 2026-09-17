package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class RoadmapItem(
    val id: String = "",
    val roadmapId: String = "",
    val title: String = "",
    val description: String = "",
    val order: Int = 0,
    val status: String = "TODO",
    val requiredSkills: List<String> = emptyList(),
    val requiredRoles: List<String> = emptyList(),
    val estimatedEffort: String = "",
    val dependencies: List<String> = emptyList(),
    val milestone: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)
