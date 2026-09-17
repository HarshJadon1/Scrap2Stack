package com.scrap2stack.app.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.NotificationItem
import com.scrap2stack.app.domain.repository.CharmsRepository
import com.scrap2stack.app.domain.repository.NotificationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Success(val notifications: List<NotificationItem>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val charmsRepository: CharmsRepository
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
                coroutineScope {
                    val notifDeferred = async { notificationRepository.getNotifications() }
                    val charmsDeferred = async { charmsRepository.getContributionHistory() }

                    val notifResult = notifDeferred.await()
                    val charmsResult = charmsDeferred.await()

                    val systemNotifs = notifResult.getOrDefault(emptyList())
                    val charmNotifs = charmsResult.getOrDefault(emptyList()).map { charm ->
                        NotificationItem(
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
                }
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
