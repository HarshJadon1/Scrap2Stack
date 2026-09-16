package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.data.repository.ProjectRepositoryImpl
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScrapAIAnalysisState {
    object Loading : ScrapAIAnalysisState()
    data class Success(val analysis: ProjectAnalysis) : ScrapAIAnalysisState()
    data class Error(val message: String) : ScrapAIAnalysisState()
}

class ScrapAIAnalysisViewModel(
    private val repository: ProjectRepository = ProjectRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScrapAIAnalysisState>(ScrapAIAnalysisState.Loading)
    val uiState: StateFlow<ScrapAIAnalysisState> = _uiState.asStateFlow()

    fun loadAnalysis(projectId: String) {
        viewModelScope.launch {
            _uiState.value = ScrapAIAnalysisState.Loading
            repository.getProjectAnalysis(projectId)
                .onSuccess { analysis ->
                    if (analysis != null) {
                        _uiState.value = ScrapAIAnalysisState.Success(analysis)
                    } else {
                        _uiState.value = ScrapAIAnalysisState.Error("No AI analysis found for this project.")
                    }
                }
                .onFailure { error ->
                    _uiState.value = ScrapAIAnalysisState.Error(error.message ?: "Failed to load AI analysis")
                }
        }
    }
}
