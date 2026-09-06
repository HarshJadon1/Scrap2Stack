package com.scrap2stack.app.data.remote.dto

data class RoadmapResponse(
    val roadmap: RoadmapDto,
    val items: List<RoadmapItemDto>
)

data class RoadmapDto(
    val id: String,
    val projectId: String,
    val workspaceId: String,
    val title: String,
    val description: String,
    val status: String,
    val generatedByAI: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class RoadmapItemDto(
    val id: String,
    val roadmapId: String,
    val title: String,
    val description: String,
    val order: Int,
    val status: String,
    val requiredSkills: List<String>,
    val requiredRoles: List<String>,
    val estimatedEffort: String,
    val dependencies: List<String>,
    val milestone: Boolean,
    val createdAt: String,
    val updatedAt: String
)
