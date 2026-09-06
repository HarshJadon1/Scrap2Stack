package com.scrap2stack.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.ProjectDto
import com.scrap2stack.app.data.remote.dto.ProjectRecommendationDto
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val recommendedProjects: List<ProjectRecommendationDto>,
        val trendingProjects: List<ProjectDto>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
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
                val recommendedResp = repository.getRecommendedProjects()
                val trendingResp = repository.getProjects(page = 1)
                
                if (recommendedResp.isSuccessful && trendingResp.isSuccessful) {
                    _uiState.value = HomeUiState.Success(
                        recommendedProjects = recommendedResp.body()?.data ?: emptyList(),
                        trendingProjects = trendingResp.body()?.data?.projects ?: emptyList()
                    )
                } else {
                    _uiState.value = HomeUiState.Error("Failed to load home data")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
