package com.scrap2stack.app.feature.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.repository.WorkspaceRepository

class WorkspaceViewModelFactory(
    private val repository: WorkspaceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkspaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkspaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
