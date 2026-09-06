package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.MatchResultDto
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MatchingState {
    object Idle : MatchingState()
    object Loading : MatchingState()
    data class Success(val matches: List<MatchResultDto>) : MatchingState()
    object Empty : MatchingState()
    data class Error(val message: String) : MatchingState()
}

class DeveloperMatchingViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchingState>(MatchingState.Idle)
    val uiState: StateFlow<MatchingState> = _uiState.asStateFlow()

    fun loadMatches(projectId: String) {
        viewModelScope.launch {
            _uiState.value = MatchingState.Loading
            try {
                val response = repository.getDeveloperMatches(projectId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val matches = response.body()?.data?.matches ?: emptyList()
                    if (matches.isEmpty()) {
                        _uiState.value = MatchingState.Empty
                    } else {
                        _uiState.value = MatchingState.Success(matches)
                    }
                } else {
                    _uiState.value = MatchingState.Error(response.body()?.message ?: "Failed to load matches")
                }
            } catch (e: Exception) {
                _uiState.value = MatchingState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
