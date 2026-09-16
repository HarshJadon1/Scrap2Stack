package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.ProfileUpdateRequest
import com.scrap2stack.app.data.remote.dto.ProfileUpsertRequest
import com.scrap2stack.app.data.remote.dto.UserDto
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
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

            if (dto == null) {
                Result.success(
                    Developer(
                        id = userId,
                        name = defaultName,
                        username = defaultUsername,
                        bio = ""
                    )
                )
            } else {
                Result.success(dto.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                        filter { eq("id", userId) }
                        select()
                    }
                    .decodeSingle<UserDto>()
            } catch (e: Exception) {
                // Fallback to UPSERT if row does not exist in profiles yet
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

            Result.success(updatedDto.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save profile: ${e.localizedMessage}"))
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
            
            Result.success(dtos.map { it.toDomain() })
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
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun UserDto.toDomain(): Developer {
        return Developer(
            id = id,
            name = name,
            username = username,
            bio = bio ?: "",
            profileImageUrl = profileImage,
            skills = skills,
            interests = interests,
            experienceLevel = try {
                ExperienceLevel.valueOf(experienceLevel?.uppercase() ?: "BEGINNER")
            } catch (e: Exception) {
                ExperienceLevel.BEGINNER
            },
            githubUrl = githubUrl ?: "",
            linkedinUrl = linkedinUrl ?: "",
            portfolioUrl = portfolioUrl ?: "",
            charms = charms,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }
}
