package com.scrap2stack.app.domain.model

data class ProjectMember(
    val id: String,
    val projectId: String,
    val userId: String,
    val role: ProjectRole,
    val joinedAt: String,
    val user: Developer? = null
)

enum class ProjectRole {
    OWNER,
    CONTRIBUTOR,
    MAINTAINER
}
