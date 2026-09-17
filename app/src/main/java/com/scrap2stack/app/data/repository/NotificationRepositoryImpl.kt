package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.NotificationDto
import com.scrap2stack.app.domain.model.NotificationItem
import com.scrap2stack.app.domain.repository.NotificationRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class NotificationRepositoryImpl : NotificationRepository {

    override suspend fun getNotifications(): Result<List<NotificationItem>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val notifications = supabase.from("notifications")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationDto>()

            Result.success(notifications.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("notifications")
                .update(buildJsonObject { put("read", true) }) {
                    filter { eq("id", notificationId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            supabase.from("notifications")
                .update(buildJsonObject { put("read", true) }) {
                    filter { eq("user_id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun NotificationDto.toDomain(): NotificationItem {
        return NotificationItem(
            id = id,
            userId = userId,
            title = title,
            content = content,
            type = type,
            read = read,
            referenceId = referenceId,
            createdAt = createdAt
        )
    }
}
