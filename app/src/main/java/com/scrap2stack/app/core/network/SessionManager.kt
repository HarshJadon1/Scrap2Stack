package com.scrap2stack.app.core.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Property delegate at top-level to ensure single DataStore instance
private val Context.dataStore by preferencesDataStore(name = "scrap2stack_session")

class SessionManager(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val ONBOARDING_COMPLETED = stringPreferencesKey("onboarding_completed")
        
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context).also { INSTANCE = it }
            }
        }
    }

    val authToken: Flow<String?> = appContext.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    val userId: Flow<String?> = appContext.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val isOnboardingCompleted: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED]?.toBoolean() ?: false
    }

    suspend fun saveAuthToken(token: String) {
        appContext.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun saveUserId(id: String) {
        appContext.dataStore.edit { preferences ->
            preferences[USER_ID] = id
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed.toString()
        }
    }

    suspend fun clearSession() {
        appContext.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(USER_ID)
        }
    }
}
