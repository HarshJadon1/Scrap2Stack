package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.ChatMessageDto
import com.scrap2stack.app.data.remote.dto.UserDto
import com.scrap2stack.app.domain.model.ChatMessage
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.repository.ChatRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class ChatRepositoryImpl : ChatRepository {

    override suspend fun getProjectMessages(projectId: String): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        try {
            val dtos = supabase.from("chat_messages")
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<ChatMessageDto>()

            val senderIds = dtos.map { it.senderId }.filter { it.isNotBlank() }.distinct()
            val sendersMap = try {
                if (senderIds.isNotEmpty()) {
                    supabase.from("developer_profiles")
                        .select { filter { isIn("id", senderIds) } }
                        .decodeList<UserDto>()
                        .associateBy { it.id }
                } else emptyMap()
            } catch (e: Exception) {
                emptyMap()
            }

            val messages = dtos.map { dto ->
                val senderDto = dto.sender ?: sendersMap[dto.senderId]
                val senderDev = senderDto?.toDomain()
                val senderName = senderDev?.name?.ifBlank { null } ?: senderDev?.username ?: "Team Member"
                dto.toDomain().copy(
                    senderName = senderName,
                    sender = senderDev
                )
            }

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(projectId: String, message: String): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val senderId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val payload = buildJsonObject {
                put("project_id", projectId)
                put("sender_id", senderId)
                put("message", message)
            }

            val createdDto = supabase.from("chat_messages")
                .insert(payload) {
                    select()
                }
                .decodeSingle<ChatMessageDto>()

            Result.success(createdDto.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send message: ${e.localizedMessage}"))
        }
    }

    override fun observeProjectMessages(projectId: String): Flow<Unit> = callbackFlow {
        val channelTopic = "chat_${projectId.take(8)}_${UUID.randomUUID().toString().take(6)}"
        val channel = supabase.realtime.channel(channelTopic)
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "chat_messages"
        }

        try {
            channel.subscribe()
        } catch (ignored: Exception) {}

        val job = launch {
            flow.collect { trySend(Unit) }
        }

        awaitClose {
            job.cancel()
            launch {
                try {
                    supabase.realtime.removeChannel(channel)
                } catch (ignored: Exception) {}
            }
        }
    }

    private fun ChatMessageDto.toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            projectId = projectId,
            senderId = senderId,
            message = message,
            createdAt = createdAt,
            sender = sender?.toDomain()
        )
    }

    private fun UserDto.toDomain(): Developer {
        return Developer(
            id = id,
            name = name,
            username = username,
            bio = bio ?: "",
            profileImageUrl = profileImage,
            skills = skills,
            interests = interests,
            experienceLevel = try {
                ExperienceLevel.valueOf(experienceLevel?.uppercase() ?: "BEGINNER")
            } catch (e: Exception) {
                ExperienceLevel.BEGINNER
            },
            githubUrl = githubUrl ?: "",
            linkedinUrl = linkedinUrl ?: "",
            portfolioUrl = portfolioUrl ?: "",
            charms = charms,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }
}
