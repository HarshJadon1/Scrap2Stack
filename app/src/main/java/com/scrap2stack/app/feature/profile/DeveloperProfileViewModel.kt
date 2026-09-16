package com.scrap2stack.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DeveloperProfileUiState {
    object Loading : DeveloperProfileUiState()
    data class Success(val developer: Developer) : DeveloperProfileUiState()
    data class Error(val message: String) : DeveloperProfileUiState()
}

class DeveloperProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeveloperProfileUiState>(DeveloperProfileUiState.Loading)
    val uiState: StateFlow<DeveloperProfileUiState> = _uiState.asStateFlow()

    fun loadDeveloperProfile(developerId: String) {
        viewModelScope.launch {
            _uiState.value = DeveloperProfileUiState.Loading
            userRepository.getDeveloperById(developerId)
                .onSuccess { developer ->
                    _uiState.value = DeveloperProfileUiState.Success(developer)
                }
                .onFailure { error ->
                    _uiState.value = DeveloperProfileUiState.Error(error.message ?: "Failed to load developer profile")
                }
        }
    }
}
