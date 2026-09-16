package com.scrap2stack.app.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.data.remote.dto.NotificationDto
import com.scrap2stack.app.data.repository.CharmsRepositoryImpl
import com.scrap2stack.app.domain.repository.CharmsRepository
import com.scrap2stack.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Success(val notifications: List<NotificationDto>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val charmsRepository: CharmsRepository = CharmsRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading
            try {
                val notifResult = notificationRepository.getNotifications()
                val charmsResult = charmsRepository.getContributionHistory()

                val systemNotifs = notifResult.getOrDefault(emptyList())
                val charmNotifs = charmsResult.getOrDefault(emptyList()).map { charm ->
                    NotificationDto(
                        id = charm.id,
                        title = "+${charm.charms} Charms Earned",
                        content = charm.description.ifEmpty { charm.contributionType.name.replace("_", " ") },
                        type = "CHARMS",
                        read = true,
                        createdAt = charm.createdAt
                    )
                }

                val merged = (systemNotifs + charmNotifs).sortedByDescending { it.createdAt }
                _uiState.value = NotificationsUiState.Success(merged)
            } catch (e: Exception) {
                _uiState.value = NotificationsUiState.Error("Failed to load activity feed: ${e.localizedMessage}")
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onSuccess { loadNotifications() }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
                .onSuccess { loadNotifications() }
        }
    }
}
