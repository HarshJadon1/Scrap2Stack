package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.AnalysisDto
import com.scrap2stack.app.data.remote.dto.ProjectDto
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProjectDetailsState {
    object Loading : ProjectDetailsState()
    data class Success(val project: ProjectDto) : ProjectDetailsState()
    data class Error(val message: String) : ProjectDetailsState()
}

sealed class ProjectAnalysisState {
    object Loading : ProjectAnalysisState()
    data class Success(val analysis: AnalysisDto) : ProjectAnalysisState()
    data class Error(val message: String) : ProjectAnalysisState()
}

class ProjectDetailsViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectDetailsState>(ProjectDetailsState.Loading)
    val uiState: StateFlow<ProjectDetailsState> = _uiState.asStateFlow()

    private val _analysisState = MutableStateFlow<ProjectAnalysisState>(ProjectAnalysisState.Loading)
    val analysisState: StateFlow<ProjectAnalysisState> = _analysisState.asStateFlow()

    fun loadProjectDetails(projectId: String) {
        viewModelScope.launch {
            _uiState.value = ProjectDetailsState.Loading
            try {
                val response = repository.getProjectDetails(projectId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = ProjectDetailsState.Success(response.body()!!.data!!)
                } else {
                    _uiState.value = ProjectDetailsState.Error(response.body()?.message ?: "Failed to load project details")
                }
            } catch (e: Exception) {
                _uiState.value = ProjectDetailsState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun loadProjectAnalysis(projectId: String) {
        viewModelScope.launch {
            _analysisState.value = ProjectAnalysisState.Loading
            try {
                val response = repository.getProjectAnalysis(projectId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _analysisState.value = ProjectAnalysisState.Success(response.body()!!.data!!)
                } else {
                    _analysisState.value = ProjectAnalysisState.Error(response.body()?.message ?: "Failed to load analysis")
                }
            } catch (e: Exception) {
                _analysisState.value = ProjectAnalysisState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
