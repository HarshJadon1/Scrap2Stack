package com.scrap2stack.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, username: String, email: String, password: String): Result<Unit>
    suspend fun logout()
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun isUserLoggedIn(): Boolean
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    fun getCurrentUserId(): String?
    fun getAccessToken(): String?
}
