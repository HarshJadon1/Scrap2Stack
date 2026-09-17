package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String = "",
    @SerialName("project_id")
    val projectId: String,
    @SerialName("sender_id")
    val senderId: String,
    val message: String,
    @SerialName("created_at")
    val createdAt: String = "",
    val sender: UserDto? = null
)
