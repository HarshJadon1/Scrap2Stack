package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.usecase.CreateProjectUseCase

class CreateProjectViewModelFactory(
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateProjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateProjectViewModel(createProjectUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
