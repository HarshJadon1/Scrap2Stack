package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubContributionDto(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("project_id")
    val projectId: String = "",
    @SerialName("repository_id")
    val repositoryId: Long = 0L,
    val type: String = "COMMIT",
    @SerialName("github_event_id")
    val githubEventId: String = "",
    val title: String = "",
    val url: String = "",
    val status: String = "MERGED",
    val verified: Boolean = true,
    val metadata: Map<String, String>? = null,
    @SerialName("contribution_date")
    val contributionDate: String = "",
    @SerialName("created_at")
    val createdAt: String = ""
)
