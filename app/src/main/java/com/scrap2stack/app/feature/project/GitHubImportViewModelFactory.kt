package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.repository.ProjectRepository

class GitHubImportViewModelFactory(
    private val repository: ProjectRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GitHubImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GitHubImportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
