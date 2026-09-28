package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.repository.UserRepository
import com.scrap2stack.app.domain.usecase.DeleteProjectUseCase
import com.scrap2stack.app.domain.usecase.GetProjectDetailsUseCase
import com.scrap2stack.app.domain.usecase.UpdateProjectUseCase

class ProjectDetailsViewModelFactory(
    private val getProjectDetailsUseCase: GetProjectDetailsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase,
    private val repository: ProjectRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectDetailsViewModel(
                getProjectDetailsUseCase,
                deleteProjectUseCase,
                updateProjectUseCase,
                repository,
                userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
