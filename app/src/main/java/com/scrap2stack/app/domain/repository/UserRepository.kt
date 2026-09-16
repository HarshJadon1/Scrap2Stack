package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.Developer

interface UserRepository {
    suspend fun getMyProfile(): Result<Developer>
    suspend fun updateProfile(developer: Developer): Result<Developer>
    suspend fun getDevelopers(): Result<List<Developer>>
    suspend fun getDeveloperById(id: String): Result<Developer>
}
