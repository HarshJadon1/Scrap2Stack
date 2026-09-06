package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.ApiService
import com.scrap2stack.app.core.network.SessionManager
import com.scrap2stack.app.data.remote.dto.*
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(request: LoginRequest): Response<BaseResponse<AuthResponse>> {
        val response = apiService.login(request)
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data?.let { authData ->
                sessionManager.saveAuthToken(authData.token)
                sessionManager.saveUserId(authData.user.id)
            }
        }
        return response
    }

    suspend fun register(request: RegisterRequest): Response<BaseResponse<AuthResponse>> {
        val response = apiService.register(request)
        if (response.isSuccessful && response.body()?.success == true) {
            response.body()?.data?.let { authData ->
                sessionManager.saveAuthToken(authData.token)
                sessionManager.saveUserId(authData.user.id)
            }
        }
        return response
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    fun getAuthToken() = sessionManager.authToken
    
    fun isOnboardingCompleted() = sessionManager.isOnboardingCompleted

    suspend fun setOnboardingCompleted(completed: Boolean) {
        sessionManager.setOnboardingCompleted(completed)
    }
}
