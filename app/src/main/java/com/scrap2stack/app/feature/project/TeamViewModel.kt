package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.ProjectMember
import com.scrap2stack.app.domain.usecase.GetProjectMembersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TeamUiState {
    object Loading : TeamUiState()
    data class Success(val members: List<ProjectMember>) : TeamUiState()
    data class Error(val message: String) : TeamUiState()
}

class TeamViewModel(
    private val getProjectMembersUseCase: GetProjectMembersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    fun loadTeam(projectId: String) {
        viewModelScope.launch {
            _uiState.value = TeamUiState.Loading
            getProjectMembersUseCase(projectId)
                .onSuccess { members ->
                    _uiState.value = TeamUiState.Success(members)
                }
                .onFailure { error ->
                    _uiState.value = TeamUiState.Error(error.message ?: "Failed to load team")
                }
        }
    }
}
