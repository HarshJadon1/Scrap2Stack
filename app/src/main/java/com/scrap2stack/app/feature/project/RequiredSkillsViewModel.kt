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

sealed class RequiredSkillsState {
    object Loading : RequiredSkillsState()
    data class Success(val analysis: ProjectAnalysis) : RequiredSkillsState()
    data class Error(val message: String) : RequiredSkillsState()
}

class RequiredSkillsViewModel(
    private val repository: ProjectRepository = ProjectRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RequiredSkillsState>(RequiredSkillsState.Loading)
    val uiState: StateFlow<RequiredSkillsState> = _uiState.asStateFlow()

    fun loadSkills(projectId: String) {
        viewModelScope.launch {
            _uiState.value = RequiredSkillsState.Loading
            repository.getProjectAnalysis(projectId)
                .onSuccess { analysis ->
                    if (analysis != null) {
                        _uiState.value = RequiredSkillsState.Success(analysis)
                    } else {
                        _uiState.value = RequiredSkillsState.Error("No skills analysis found for this project.")
                    }
                }
                .onFailure { error ->
                    _uiState.value = RequiredSkillsState.Error(error.message ?: "Failed to load required skills")
                }
        }
    }
}
