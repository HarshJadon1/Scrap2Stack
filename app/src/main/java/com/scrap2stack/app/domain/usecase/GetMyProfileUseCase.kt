package com.scrap2stack.app.domain.usecase

import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.repository.UserRepository

class GetMyProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<Developer> {
        return repository.getMyProfile()
    }
}
