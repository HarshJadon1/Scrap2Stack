package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.AnalysisDto
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScrapAIAnalysisState {
    object Loading : ScrapAIAnalysisState()
    data class Success(val analysis: AnalysisDto) : ScrapAIAnalysisState()
    data class Error(val message: String) : ScrapAIAnalysisState()
}

class ScrapAIAnalysisViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScrapAIAnalysisState>(ScrapAIAnalysisState.Loading)
    val uiState: StateFlow<ScrapAIAnalysisState> = _uiState.asStateFlow()

    fun loadAnalysis(projectId: String) {
        viewModelScope.launch {
            _uiState.value = ScrapAIAnalysisState.Loading
            try {
                val response = repository.getProjectAnalysis(projectId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = ScrapAIAnalysisState.Success(response.body()!!.data!!)
                } else {
                    _uiState.value = ScrapAIAnalysisState.Error(response.body()?.message ?: "Failed to load AI analysis")
                }
            } catch (e: Exception) {
                _uiState.value = ScrapAIAnalysisState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
