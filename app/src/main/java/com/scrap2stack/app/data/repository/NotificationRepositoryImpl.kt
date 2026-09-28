package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.local.cache.AppCache
import com.scrap2stack.app.data.mapper.*
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

            val items = notifications.map { it.toDomain() }
            AppCache.saveNotifications(items)
            Result.success(items)
        } catch (e: Exception) {
            val cached = AppCache.getNotifications()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("notifications")
                .update(buildJsonObject { put("read", true) }) {
                    filter { eq("id", notificationId) }
                }
            AppCache.markNotificationAsRead(notificationId)
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
            val current = AppCache.getNotifications().map { it.copy(read = true) }
            AppCache.saveNotifications(current)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
