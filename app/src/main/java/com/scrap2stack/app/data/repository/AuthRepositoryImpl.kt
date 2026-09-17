package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.SessionManager
import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                sessionManager.saveAuthToken(session.accessToken)
                sessionManager.saveUserId(session.user?.id ?: "")
                sessionManager.setOnboardingCompleted(true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to establish session after login."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Login failed"))
        }
    }

    override suspend fun register(name: String, username: String, email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", name)
                    put("username", username)
                }
            }

            // Attempt explicit login if registration did not automatically establish a session
            if (supabase.auth.currentSessionOrNull() == null) {
                try {
                    supabase.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                } catch (ignored: Exception) {
                    // Sign in failed, email verification may be required by Supabase project configuration
                }
            }

            val session = supabase.auth.currentSessionOrNull()
            if (session != null) {
                sessionManager.saveAuthToken(session.accessToken)
                sessionManager.saveUserId(session.user?.id ?: "")
                sessionManager.setOnboardingCompleted(true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Account created! Please check your email to confirm your account before logging in."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration failed"))
        }
    }

    override suspend fun logout() {
        try {
            supabase.auth.signOut()
            sessionManager.clearSession()
        } catch (e: Exception) {
            // Log error
        }
    }
    
    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to send reset email"))
        }
    }

    override suspend fun isUserLoggedIn(): Boolean {
        val status = supabase.auth.sessionStatus.filter { it !is SessionStatus.Initializing }.first()
        return status is SessionStatus.Authenticated || supabase.auth.currentSessionOrNull() != null
    }

    override fun isOnboardingCompleted(): Flow<Boolean> = sessionManager.isOnboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        sessionManager.setOnboardingCompleted(completed)
    }

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }
    
    override fun getAccessToken(): String? {
        return supabase.auth.currentSessionOrNull()?.accessToken
    }
}
