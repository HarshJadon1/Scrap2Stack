package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.usecase.GetDeveloperMatchesUseCase

class DeveloperMatchingViewModelFactory(
    private val getDeveloperMatchesUseCase: GetDeveloperMatchesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeveloperMatchingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeveloperMatchingViewModel(getDeveloperMatchesUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
