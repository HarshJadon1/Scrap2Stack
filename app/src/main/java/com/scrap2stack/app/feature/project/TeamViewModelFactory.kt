package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.usecase.GetProjectMembersUseCase

class TeamViewModelFactory(
    private val getProjectMembersUseCase: GetProjectMembersUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeamViewModel(getProjectMembersUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
