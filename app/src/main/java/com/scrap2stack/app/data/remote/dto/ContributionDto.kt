package com.scrap2stack.app.data.remote.dto

data class GitHubContributionDto(
    val id: String,
    val userId: String,
    val projectId: String,
    val repositoryId: Long,
    val type: String,
    val githubEventId: String,
    val title: String,
    val url: String,
    val status: String,
    val verified: Boolean,
    val metadata: Map<String, String>?,
    val contributionDate: String,
    val createdAt: String
)
