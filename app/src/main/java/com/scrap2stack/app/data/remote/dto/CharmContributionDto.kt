package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharmContributionDto(
    val id: String = "",
    @SerialName("user_id")
    val userId: String,
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("contribution_type")
    val contributionType: String,
    val charms: Int,
    val description: String,
    @SerialName("reference_id")
    val referenceId: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("projects")
    val project: ProjectNameDto? = null
)

@Serializable
data class ProjectNameDto(
    val name: String
)
