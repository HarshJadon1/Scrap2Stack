package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.NotificationItem

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationItem>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}
