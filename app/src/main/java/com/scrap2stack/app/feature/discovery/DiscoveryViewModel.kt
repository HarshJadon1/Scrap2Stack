package com.scrap2stack.app.feature.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DiscoveryUiState {
    object Loading : DiscoveryUiState()
    data class Success(val projects: List<Project>) : DiscoveryUiState()
    object Empty : DiscoveryUiState()
    data class Error(val message: String) : DiscoveryUiState()
}

class DiscoveryViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Loading)
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = DiscoveryUiState.Loading
            repository.getDiscoverProjects()
                .onSuccess { projects ->
                    if (projects.isEmpty()) {
                        _uiState.value = DiscoveryUiState.Empty
                    } else {
                        _uiState.value = DiscoveryUiState.Success(projects)
                    }
                }
                .onFailure { error ->
                    _uiState.value = DiscoveryUiState.Error(error.message ?: "Failed to load projects")
                }
        }
    }
}
