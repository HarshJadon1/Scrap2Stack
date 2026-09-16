package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.SessionManager
import com.scrap2stack.app.core.network.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            sessionManager.setOnboardingCompleted(true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, username: String, email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", name)
                    put("username", username)
                }
            }
            sessionManager.setOnboardingCompleted(true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            supabase.auth.signOut()
            sessionManager.clearSession()
        } catch (e: Exception) {
            // Log error
        }
    }
    
    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if the user is currently logged in.
     * Waits for session restoration if it's still initializing.
     */
    suspend fun isUserLoggedIn(): Boolean {
        // Wait until session is no longer in Initializing state
        val status = supabase.auth.sessionStatus.filter { it !is SessionStatus.Initializing }.first()
        return status is SessionStatus.Authenticated || supabase.auth.currentSessionOrNull() != null
    }

    fun isOnboardingCompleted(): Flow<Boolean> = sessionManager.isOnboardingCompleted

    suspend fun setOnboardingCompleted(completed: Boolean) {
        sessionManager.setOnboardingCompleted(completed)
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }
    
    fun getAccessToken(): String? {
        return supabase.auth.currentSessionOrNull()?.accessToken
    }
}
