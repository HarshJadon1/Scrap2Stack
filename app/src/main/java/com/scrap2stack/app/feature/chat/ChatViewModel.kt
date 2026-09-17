package com.scrap2stack.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.ChatMessage
import com.scrap2stack.app.domain.repository.ChatRepository
import com.scrap2stack.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(
        val messages: List<ChatMessage>,
        val currentUserId: String
    ) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadMessages(projectId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getMyProfile().getOrNull()?.id ?: ""
                val messagesResult = chatRepository.getProjectMessages(projectId)

                _uiState.value = ChatUiState.Success(
                    messages = messagesResult.getOrDefault(emptyList()),
                    currentUserId = currentUserId
                )
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.localizedMessage ?: "Failed to load chat messages")
            }
        }
    }

    fun subscribeToRealtimeChat(projectId: String) {
        viewModelScope.launch {
            chatRepository.observeProjectMessages(projectId).collect {
                loadMessages(projectId)
            }
        }
    }

    fun sendMessage(projectId: String, messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            chatRepository.sendMessage(projectId, messageText.trim())
                .onSuccess {
                    loadMessages(projectId)
                }
        }
    }
}
