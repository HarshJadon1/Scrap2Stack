package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.RevivalScoreDto
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RevivalScoreState {
    object Loading : RevivalScoreState()
    data class Success(val data: RevivalScoreDto) : RevivalScoreState()
    data class Error(val message: String) : RevivalScoreState()
}

class RevivalScoreViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<RevivalScoreState>(RevivalScoreState.Loading)
    val uiState: StateFlow<RevivalScoreState> = _uiState.asStateFlow()

    fun loadRevivalScore(projectId: String) {
        viewModelScope.launch {
            _uiState.value = RevivalScoreState.Loading
            try {
                val response = repository.getRevivalScore(projectId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = RevivalScoreState.Success(response.body()!!.data!!)
                } else {
                    _uiState.value = RevivalScoreState.Error(response.body()?.message ?: "Failed to load revival score")
                }
            } catch (e: Exception) {
                _uiState.value = RevivalScoreState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}
