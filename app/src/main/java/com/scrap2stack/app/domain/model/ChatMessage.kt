package com.scrap2stack.app.domain.model

data class ChatMessage(
    val id: String = "",
    val projectId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val createdAt: String = "",
    val sender: Developer? = null
)
