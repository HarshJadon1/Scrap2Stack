package com.scrap2stack.app.data.remote.dto

data class CollaborationRequestDto(
    val id: String,
    val projectId: String,
    val senderId: String,
    val receiverId: String,
    val proposedRole: String,
    val message: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val project: ProjectDto? = null,
    val sender: UserDto? = null,
    val receiver: UserDto? = null
)

data class SendCollabRequest(
    val receiverId: String,
    val proposedRole: String,
    val message: String
)
