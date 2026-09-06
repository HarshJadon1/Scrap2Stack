package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.CollaborationRequestDto
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CollaborationRequestsState {
    object Loading : CollaborationRequestsState()
    data class Success(val requests: List<CollaborationRequestDto>) : CollaborationRequestsState()
    object Empty : CollaborationRequestsState()
    data class Error(val message: String) : CollaborationRequestsState()
}

class CollaborationRequestsViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollaborationRequestsState>(CollaborationRequestsState.Loading)
    val uiState: StateFlow<CollaborationRequestsState> = _uiState.asStateFlow()

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.value = CollaborationRequestsState.Loading
            try {
                val response = repository.getReceivedRequests()
                if (response.isSuccessful && response.body()?.success == true) {
                    val requests = response.body()?.data ?: emptyList()
                    if (requests.isEmpty()) {
                        _uiState.value = CollaborationRequestsState.Empty
                    } else {
                        _uiState.value = CollaborationRequestsState.Success(requests)
                    }
                } else {
                    _uiState.value = CollaborationRequestsState.Error(response.body()?.message ?: "Failed to load requests")
                }
            } catch (e: Exception) {
                _uiState.value = CollaborationRequestsState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val response = repository.acceptRequest(requestId)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadRequests() // Refresh
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val response = repository.rejectRequest(requestId)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadRequests() // Refresh
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
