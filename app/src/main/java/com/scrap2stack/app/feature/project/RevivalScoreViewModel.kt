package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.data.repository.ProjectRepositoryImpl
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RevivalScoreState {
    object Loading : RevivalScoreState()
    data class Success(val project: Project) : RevivalScoreState()
    data class Error(val message: String) : RevivalScoreState()
}

class RevivalScoreViewModel(
    private val repository: ProjectRepository = ProjectRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevivalScoreState>(RevivalScoreState.Loading)
    val uiState: StateFlow<RevivalScoreState> = _uiState.asStateFlow()

    fun loadRevivalScore(projectId: String) {
        viewModelScope.launch {
            _uiState.value = RevivalScoreState.Loading
            repository.getProjectById(projectId)
                .onSuccess { project ->
                    _uiState.value = RevivalScoreState.Success(project)
                }
                .onFailure { error ->
                    _uiState.value = RevivalScoreState.Error(error.message ?: "Failed to load revival score")
                }
        }
    }
}
