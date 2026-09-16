package com.scrap2stack.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserPreferences(
    val isDarkMode: Boolean = true,
    val isPushNotificationsEnabled: Boolean = true,
    val availabilityStatus: String = "Available",
    val projectInterests: String = "Mobile, AI/ML, Web3",
    val skillMatchingPreferences: String = "Kotlin, Python, React"
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val AVAILABILITY_STATUS = stringPreferencesKey("availability_status")
        val PROJECT_INTERESTS = stringPreferencesKey("project_interests")
        val SKILL_MATCHING_PREFERENCES = stringPreferencesKey("skill_matching_preferences")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.userSettingsDataStore.data
        .map { preferences ->
            UserPreferences(
                isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: true,
                isPushNotificationsEnabled = preferences[PreferencesKeys.PUSH_NOTIFICATIONS] ?: true,
                availabilityStatus = preferences[PreferencesKeys.AVAILABILITY_STATUS] ?: "Available",
                projectInterests = preferences[PreferencesKeys.PROJECT_INTERESTS] ?: "Mobile, AI/ML, Web3",
                skillMatchingPreferences = preferences[PreferencesKeys.SKILL_MATCHING_PREFERENCES] ?: "Kotlin, Python, React"
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun setPushNotifications(enabled: Boolean) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.PUSH_NOTIFICATIONS] = enabled
        }
    }

    suspend fun setAvailabilityStatus(status: String) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.AVAILABILITY_STATUS] = status
        }
    }

    suspend fun setProjectInterests(interests: String) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.PROJECT_INTERESTS] = interests
        }
    }

    suspend fun setSkillMatchingPreferences(skills: String) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SKILL_MATCHING_PREFERENCES] = skills
        }
    }
}
