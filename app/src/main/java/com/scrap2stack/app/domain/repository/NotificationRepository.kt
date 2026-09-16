package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.data.remote.dto.NotificationDto

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationDto>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}
