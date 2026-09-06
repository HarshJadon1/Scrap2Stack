package com.scrap2stack.app.data.remote.dto

data class GitHubImportRequest(
    val repositoryUrl: String
)

data class ImportResponse(
    val project: ProjectDto,
    val analysis: AnalysisDto
)
