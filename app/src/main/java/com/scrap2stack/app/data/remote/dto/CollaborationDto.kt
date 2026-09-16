package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollaborationRequestDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    @SerialName("proposed_role")
    val proposedRole: String = "CONTRIBUTOR",
    val message: String,
    val status: String = "PENDING",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    val project: ProjectDto? = null,
    val sender: UserDto? = null,
    val receiver: UserDto? = null
)

@Serializable
data class SendCollabRequest(
    @SerialName("project_id")
    val projectId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    @SerialName("proposed_role")
    val proposedRole: String,
    val message: String
)
