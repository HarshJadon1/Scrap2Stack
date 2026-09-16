package com.scrap2stack.app.domain.model

data class CollaborationRequest(
    val id: String,
    val projectId: String,
    val senderId: String,
    val receiverId: String,
    val proposedRole: String,
    val message: String,
    val status: CollaborationStatus,
    val createdAt: String,
    val project: Project? = null,
    val sender: Developer? = null,
    val receiver: Developer? = null
)

enum class CollaborationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}
