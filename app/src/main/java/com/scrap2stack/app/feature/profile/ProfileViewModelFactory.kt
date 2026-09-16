package com.scrap2stack.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.usecase.GetMyProfileUseCase
import com.scrap2stack.app.domain.usecase.UpdateProfileUseCase

class ProfileViewModelFactory(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(getMyProfileUseCase, updateProfileUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
