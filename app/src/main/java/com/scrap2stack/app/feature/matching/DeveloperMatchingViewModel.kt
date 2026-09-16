package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.DeveloperMatch
import com.scrap2stack.app.domain.usecase.GetDeveloperMatchesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MatchingUiState {
    object Idle : MatchingUiState()
    object Loading : MatchingUiState()
    data class Success(val matches: List<DeveloperMatch>) : MatchingUiState()
    object Empty : MatchingUiState()
    data class Error(val message: String) : MatchingUiState()
}

class DeveloperMatchingViewModel(
    private val getDeveloperMatchesUseCase: GetDeveloperMatchesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchingUiState>(MatchingUiState.Idle)
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    fun loadMatches(projectId: String) {
        viewModelScope.launch {
            _uiState.value = MatchingUiState.Loading
            getDeveloperMatchesUseCase(projectId)
                .onSuccess { matches ->
                    if (matches.isEmpty()) {
                        _uiState.value = MatchingUiState.Empty
                    } else {
                        _uiState.value = MatchingUiState.Success(matches)
                    }
                }
                .onFailure { error ->
                    _uiState.value = MatchingUiState.Error(error.message ?: "Failed to load matches")
                }
        }
    }
}
