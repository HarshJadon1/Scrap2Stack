package com.scrap2stack.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val type: String = "SYSTEM",
    val read: Boolean = false,
    @SerialName("reference_id")
    val referenceId: String? = null,
    @SerialName("created_at")
    val createdAt: String = ""
)
