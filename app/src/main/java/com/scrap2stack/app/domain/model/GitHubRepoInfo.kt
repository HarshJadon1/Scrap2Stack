package com.scrap2stack.app.domain.model

data class GitHubRepoInfo(
    val owner: String,
    val name: String,
    val description: String?,
    val url: String,
    val defaultBranch: String,
    val primaryLanguage: String?,
    val stars: Int,
    val forks: Int,
    val openIssues: Int,
    val updatedAt: String
)
