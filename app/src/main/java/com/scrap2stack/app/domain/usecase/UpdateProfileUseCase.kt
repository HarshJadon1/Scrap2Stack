package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.repository.UserRepository

class UpdateProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(developer: Developer): Result<Developer> {
        return repository.updateProfile(developer)
    }
}
