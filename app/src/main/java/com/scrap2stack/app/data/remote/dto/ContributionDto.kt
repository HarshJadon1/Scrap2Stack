package com.scrap2stack.app.data.remote.dto

data class GitHubContributionDto(
    val id: String = "",
    val userId: String = "",
    val projectId: String = "",
    val repositoryId: Long = 0L,
    val type: String = "COMMIT",
    val githubEventId: String = "",
    val title: String = "",
    val url: String = "",
    val status: String = "MERGED",
    val verified: Boolean = true,
    val metadata: Map<String, String>? = null,
    val contributionDate: String = "",
    val createdAt: String = ""
)
