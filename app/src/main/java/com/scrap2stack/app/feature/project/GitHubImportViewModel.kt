package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.BaseResponse
import com.scrap2stack.app.data.remote.dto.ImportResponse
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GitHubImportState {
    object Idle : GitHubImportState()
    data class Loading(val message: String) : GitHubImportState()
    data class Success(val data: ImportResponse) : GitHubImportState()
    data class Error(val message: String) : GitHubImportState()
}

class GitHubImportViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<GitHubImportState>(GitHubImportState.Idle)
    val uiState: StateFlow<GitHubImportState> = _uiState.asStateFlow()

    fun importRepository(url: String) {
        viewModelScope.launch {
            _uiState.value = GitHubImportState.Loading("Connecting to GitHub...")
            try {
                // Simulate progressive loading messages as per requirements
                kotlinx.coroutines.delay(800)
                _uiState.value = GitHubImportState.Loading("Reading project information...")
                kotlinx.coroutines.delay(800)
                _uiState.value = GitHubImportState.Loading("ScrapAI is analyzing the project...")
                
                val response = repository.importRepository(url)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = GitHubImportState.Success(response.body()!!.data!!)
                } else {
                    val errorMessage = response.body()?.message ?: "GitHub repository could not be accessed"
                    _uiState.value = GitHubImportState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _uiState.value = GitHubImportState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _uiState.value = GitHubImportState.Idle
    }
}
