package com.scrap2stack.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val type: String = "SYSTEM",
    val read: Boolean = false,
    val referenceId: String? = null,
    val createdAt: String = ""
)
