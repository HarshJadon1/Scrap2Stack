package com.scrap2stack.app.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.data.local.UserPreferences
import com.scrap2stack.app.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserPreferencesRepository(application)

    val userPreferencesState: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDarkMode(enabled)
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            repository.setPushNotifications(enabled)
        }
    }

    fun updateAvailabilityStatus(status: String) {
        viewModelScope.launch {
            repository.setAvailabilityStatus(status)
        }
    }

    fun updateProjectInterests(interests: String) {
        viewModelScope.launch {
            repository.setProjectInterests(interests)
        }
    }

    fun updateSkillMatchingPreferences(skills: String) {
        viewModelScope.launch {
            repository.setSkillMatchingPreferences(skills)
        }
    }
}
