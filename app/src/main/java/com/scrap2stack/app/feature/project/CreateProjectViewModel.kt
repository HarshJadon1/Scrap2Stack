package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.domain.usecase.CreateProjectUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateProjectUiState {
    object Idle : CreateProjectUiState()
    object Loading : CreateProjectUiState()
    data class Success(val project: Project) : CreateProjectUiState()
    data class Error(val message: String) : CreateProjectUiState()
}

class CreateProjectViewModel(
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateProjectUiState>(CreateProjectUiState.Idle)
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    fun createProject(
        name: String,
        problem: String,
        description: String,
        category: String = "Web",
        teamSize: Int = 4,
        technologies: List<String>,
        status: ProjectStatus,
        requiredSkills: List<String>,
        githubUrl: String = ""
    ) {
        if (name.isBlank() || problem.isBlank() || description.isBlank() || technologies.isEmpty() || requiredSkills.isEmpty()) {
            _uiState.value = CreateProjectUiState.Error("Please fill all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreateProjectUiState.Loading
            val project = Project(
                name = name,
                problem = problem,
                description = description,
                category = category,
                teamSize = if (teamSize > 0) teamSize else 4,
                technologies = technologies,
                status = status,
                requiredSkills = requiredSkills,
                githubUrl = githubUrl
            )
            
            createProjectUseCase(project)
                .onSuccess { createdProject ->
                    _uiState.value = CreateProjectUiState.Success(createdProject)
                }
                .onFailure { error ->
                    _uiState.value = CreateProjectUiState.Error(error.message ?: "Failed to create project")
                }
        }
    }

    fun resetState() {
        _uiState.value = CreateProjectUiState.Idle
    }
}
