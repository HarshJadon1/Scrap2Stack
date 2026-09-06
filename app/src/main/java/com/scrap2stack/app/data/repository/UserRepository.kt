package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.ApiService
import com.scrap2stack.app.data.remote.dto.*
import retrofit2.Response

class UserRepository(private val apiService: ApiService) {
    suspend fun getMyProfile(): Response<BaseResponse<UserDto>> {
        return apiService.getMyProfile()
    }

    suspend fun updateProfile(updateData: Map<String, Any>): Response<BaseResponse<UserDto>> {
        return apiService.updateProfile(updateData)
    }
}
