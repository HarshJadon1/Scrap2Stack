package com.scrap2stack.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.usecase.GetMyProfileUseCase
import com.scrap2stack.app.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val developer: Developer) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class EditProfileUiState {
    object Idle : EditProfileUiState()
    object Loading : EditProfileUiState()
    object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}

class ProfileViewModel(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _editUiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val editUiState: StateFlow<EditProfileUiState> = _editUiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            getMyProfileUseCase()
                .onSuccess { developer ->
                    _uiState.value = ProfileUiState.Success(developer)
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Error(error.message ?: "Failed to load profile")
                }
        }
    }

    fun updateProfile(
        name: String,
        username: String,
        bio: String,
        skills: List<String>,
        interests: List<String>,
        experienceLevel: ExperienceLevel,
        githubUrl: String,
        linkedinUrl: String,
        portfolioUrl: String
    ) {
        val currentDeveloper = (uiState.value as? ProfileUiState.Success)?.developer ?: return

        viewModelScope.launch {
            _editUiState.value = EditProfileUiState.Loading
            val updatedDeveloper = currentDeveloper.copy(
                name = name,
                username = username,
                bio = bio,
                skills = skills,
                interests = interests,
                experienceLevel = experienceLevel,
                githubUrl = githubUrl,
                linkedinUrl = linkedinUrl,
                portfolioUrl = portfolioUrl
            )

            updateProfileUseCase(updatedDeveloper)
                .onSuccess {
                    _editUiState.value = EditProfileUiState.Success
                    _uiState.value = ProfileUiState.Success(it)
                }
                .onFailure { error ->
                    _editUiState.value = EditProfileUiState.Error(error.message ?: "Failed to update profile")
                }
        }
    }

    fun resetEditState() {
        _editUiState.value = EditProfileUiState.Idle
    }
}
