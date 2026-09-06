package com.scrap2stack.app.data.remote.dto

data class WorkspaceDto(
    val id: String,
    val projectId: String,
    val teamId: String,
    val name: String,
    val description: String?,
    val status: String,
    val progress: Double,
    val currentPhase: String,
    val createdAt: String,
    val updatedAt: String
)
