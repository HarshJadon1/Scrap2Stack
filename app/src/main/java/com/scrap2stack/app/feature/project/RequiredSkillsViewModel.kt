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

sealed class RequiredSkillsState {
    object Loading : RequiredSkillsState()
    data class Success(val analysis: AnalysisDto) : RequiredSkillsState()
    data class Error(val message: String) : RequiredSkillsState()
}

class RequiredSkillsViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<RequiredSkillsState>(RequiredSkillsState.Loading)
    val uiState: StateFlow<RequiredSkillsState> = _uiState.asStateFlow()

    fun loadSkills(projectId: String) {
        viewModelScope.launch {
            _uiState.value = RequiredSkillsState.Loading
            try {
                val response = repository.getProjectAnalysis(projectId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = RequiredSkillsState.Success(response.body()!!.data!!)
                } else {
                    _uiState.value = RequiredSkillsState.Error(response.body()?.message ?: "Failed to load skills")
                }
            } catch (e: Exception) {
                _uiState.value = RequiredSkillsState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
