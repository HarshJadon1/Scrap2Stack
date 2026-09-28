package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubImportRequest(
    @SerialName("repository_url")
    val repositoryUrl: String
)

@Serializable
data class ImportResponse(
    val project: ProjectDto,
    val analysis: AnalysisDto
)
