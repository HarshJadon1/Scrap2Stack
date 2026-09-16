package com.scrap2stack.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.data.remote.dto.ProjectDto
import com.scrap2stack.app.data.remote.dto.ProjectRecommendationDto
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val user: Developer?,
        val recommendedProjects: List<Project>,
        val trendingProjects: List<Project>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val userResult = userRepository.getMyProfile()
                val recommendedResult = projectRepository.getFeaturedProjects()
                val trendingResult = projectRepository.getRecentProjects()
                
                if (recommendedResult.isSuccess && trendingResult.isSuccess) {
                    _uiState.value = HomeUiState.Success(
                        user = userResult.getOrNull(),
                        recommendedProjects = recommendedResult.getOrDefault(emptyList()),
                        trendingProjects = trendingResult.getOrDefault(emptyList())
                    )
                } else {
                    val errorMessage = recommendedResult.exceptionOrNull()?.message 
                        ?: trendingResult.exceptionOrNull()?.message 
                        ?: "Failed to load home data"
                    _uiState.value = HomeUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Unexpected error: ${e.localizedMessage}")
            }
        }
    }
}
