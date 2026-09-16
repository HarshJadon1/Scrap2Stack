package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.ProjectDto
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.usecase.DeleteProjectUseCase
import com.scrap2stack.app.domain.usecase.GetProjectDetailsUseCase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProjectDetailsState {
    object Loading : ProjectDetailsState()
    data class Success(val project: ProjectDto, val isOwner: Boolean, val isSaved: Boolean) : ProjectDetailsState()
    data class Error(val message: String) : ProjectDetailsState()
    object Deleted : ProjectDetailsState()
}

sealed class ProjectAnalysisState {
    object Loading : ProjectAnalysisState()
    data class Success(val analysis: ProjectAnalysis) : ProjectAnalysisState()
    data class Error(val message: String) : ProjectAnalysisState()
}

class ProjectDetailsViewModel(
    private val getProjectDetailsUseCase: GetProjectDetailsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectDetailsState>(ProjectDetailsState.Loading)
    val uiState: StateFlow<ProjectDetailsState> = _uiState.asStateFlow()

    private val _analysisState = MutableStateFlow<ProjectAnalysisState>(ProjectAnalysisState.Loading)
    val analysisState: StateFlow<ProjectAnalysisState> = _analysisState.asStateFlow()

    fun loadProjectDetails(projectId: String) {
        viewModelScope.launch {
            _uiState.value = ProjectDetailsState.Loading
            getProjectDetailsUseCase(projectId)
                .onSuccess { project ->
                    val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id
                    val isOwner = project.ownerId == currentUserId
                    val isSavedResult = repository.isProjectSaved(projectId)
                    val isSaved = isSavedResult.getOrDefault(false)
                    
                    _uiState.value = ProjectDetailsState.Success(
                        project = ProjectDto(
                            id = project.id,
                            ownerId = project.ownerId,
                            name = project.name,
                            description = project.description,
                            problem = project.problem,
                            technologies = project.technologies,
                            requiredSkills = project.requiredSkills,
                            status = project.status.name,
                            revivalScore = project.revivalScore,
                            teamSize = project.teamSize,
                            githubUrl = project.githubUrl
                        ),
                        isOwner = isOwner,
                        isSaved = isSaved
                    )
                }
                .onFailure { error ->
                    _uiState.value = ProjectDetailsState.Error(error.message ?: "Failed to load project details")
                }
        }
    }

    fun toggleSave(projectId: String) {
        val currentState = _uiState.value as? ProjectDetailsState.Success ?: return
        viewModelScope.launch {
            val currentlySaved = currentState.isSaved
            val result = if (currentlySaved) {
                repository.unsaveProject(projectId)
            } else {
                repository.saveProject(projectId)
            }

            if (result.isSuccess) {
                _uiState.value = currentState.copy(isSaved = !currentlySaved)
            }
        }
    }

    fun loadProjectAnalysis(projectId: String) {
        viewModelScope.launch {
            _analysisState.value = ProjectAnalysisState.Loading
            repository.getProjectAnalysis(projectId)
                .onSuccess { analysis ->
                    if (analysis != null) {
                        _analysisState.value = ProjectAnalysisState.Success(analysis)
                    } else {
                        _analysisState.value = ProjectAnalysisState.Error("No AI analysis found for this project.")
                    }
                }
                .onFailure { error ->
                    _analysisState.value = ProjectAnalysisState.Error(error.message ?: "Failed to load AI analysis")
                }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = ProjectDetailsState.Loading
            deleteProjectUseCase(projectId)
                .onSuccess {
                    _uiState.value = ProjectDetailsState.Deleted
                }
                .onFailure { error ->
                    _uiState.value = ProjectDetailsState.Error(error.message ?: "Failed to delete project")
                }
        }
    }
}
