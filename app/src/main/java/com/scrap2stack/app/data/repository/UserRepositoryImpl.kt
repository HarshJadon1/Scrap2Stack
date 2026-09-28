package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.core.network.toUserFriendlyMessage
import com.scrap2stack.app.data.local.cache.AppCache
import com.scrap2stack.app.data.mapper.*
import com.scrap2stack.app.data.remote.dto.ProfileUpdateRequest
import com.scrap2stack.app.data.remote.dto.ProfileUpsertRequest
import com.scrap2stack.app.data.remote.dto.UserDto
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.repository.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl : UserRepository {

    override suspend fun getMyProfile(): Result<Developer> = withContext(Dispatchers.IO) {
        try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val userEmail = session.user?.email ?: ""
            val defaultUsername = if (userEmail.contains("@")) userEmail.substringBefore("@") else "user_${userId.take(6)}"
            val defaultName = defaultUsername.replaceFirstChar { it.uppercase() }

            val dto = supabase.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<UserDto>()

            val profile = if (dto == null) {
                Developer(
                    id = userId,
                    name = defaultName,
                    username = defaultUsername,
                    bio = ""
                )
            } else {
                dto.toDomain()
            }
            AppCache.saveUser(profile)
            Result.success(profile)
        } catch (e: Exception) {
            val session = supabase.auth.currentSessionOrNull()
            val cached = session?.user?.id?.let { AppCache.getUser(it) }
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateProfile(developer: Developer): Result<Developer> = withContext(Dispatchers.IO) {
        try {
            val session = supabase.auth.currentSessionOrNull()
            val userId = session?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val userEmail = session.user?.email ?: ""
            val fallbackUsername = if (userEmail.contains("@")) userEmail.substringBefore("@") else "user_${userId.take(6)}"

            val name = developer.name.ifBlank { fallbackUsername }
            val username = developer.username.ifBlank { fallbackUsername }

            val updateRequest = ProfileUpdateRequest(
                name = name,
                username = username,
                bio = developer.bio.ifBlank { null },
                profileImage = developer.profileImageUrl,
                experienceLevel = developer.experienceLevel.name,
                skills = developer.skills,
                interests = developer.interests,
                githubUrl = developer.githubUrl.ifBlank { null },
                linkedinUrl = developer.linkedinUrl.ifBlank { null },
                portfolioUrl = developer.portfolioUrl.ifBlank { null }
            )

            val updatedDto = try {
                supabase.from("profiles")
                    .update(updateRequest) {
                        filter {
                            eq("id", userId)
                        }
                        select()
                    }
                    .decodeSingle<UserDto>()
            } catch (e: Exception) {
                // If update returned 0 rows, use upsert
                val upsertRequest = ProfileUpsertRequest(
                    id = userId,
                    name = name,
                    username = username,
                    bio = developer.bio.ifBlank { null },
                    profileImage = developer.profileImageUrl,
                    experienceLevel = developer.experienceLevel.name,
                    skills = developer.skills,
                    interests = developer.interests,
                    githubUrl = developer.githubUrl.ifBlank { null },
                    linkedinUrl = developer.linkedinUrl.ifBlank { null },
                    portfolioUrl = developer.portfolioUrl.ifBlank { null }
                )
                supabase.from("profiles")
                    .upsert(upsertRequest) {
                        select()
                    }
                    .decodeSingle<UserDto>()
            }

            val saved = updatedDto.toDomain()
            AppCache.saveUser(saved)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(Exception(e.toUserFriendlyMessage("Failed to save profile.")))
        }
    }

    override suspend fun getDevelopers(): Result<List<Developer>> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id
            
            val dtos = supabase.from("developer_profiles")
                .select {
                    if (currentUserId != null) {
                        filter {
                            neq("id", currentUserId)
                        }
                    }
                }
                .decodeList<UserDto>()
            
            val developers = dtos.map { it.toDomain() }
            developers.forEach { AppCache.saveUser(it) }
            Result.success(developers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDeveloperById(id: String): Result<Developer> = withContext(Dispatchers.IO) {
        try {
            val dto = supabase.from("developer_profiles")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<UserDto>()
            val developer = dto.toDomain()
            AppCache.saveUser(developer)
            Result.success(developer)
        } catch (e: Exception) {
            val cached = AppCache.getUser(id)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
}
