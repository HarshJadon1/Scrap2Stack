package com.scrap2stack.app.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.data.repository.CharmsRepositoryImpl
import com.scrap2stack.app.domain.repository.CharmsRepository
import com.scrap2stack.app.domain.repository.NotificationRepository

class NotificationsViewModelFactory(
    private val notificationRepository: NotificationRepository,
    private val charmsRepository: CharmsRepository = CharmsRepositoryImpl()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(notificationRepository, charmsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
