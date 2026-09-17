package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getProjectMessages(projectId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(projectId: String, message: String): Result<ChatMessage>
    fun observeProjectMessages(projectId: String): Flow<Unit>
}
