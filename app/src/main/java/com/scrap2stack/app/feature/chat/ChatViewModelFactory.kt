package com.scrap2stack.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.repository.ChatRepository
import com.scrap2stack.app.domain.repository.UserRepository

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
