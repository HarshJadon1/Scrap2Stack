package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.CollaborationRequest
import com.scrap2stack.app.domain.usecase.AcceptCollaborationRequestUseCase
import com.scrap2stack.app.domain.usecase.GetReceivedCollaborationRequestsUseCase
import com.scrap2stack.app.domain.usecase.RejectCollaborationRequestUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CollaborationRequestsState {
    object Loading : CollaborationRequestsState()
    data class Success(val requests: List<CollaborationRequest>) : CollaborationRequestsState()
    object Empty : CollaborationRequestsState()
    data class Error(val message: String) : CollaborationRequestsState()
}

class CollaborationRequestsViewModel(
    private val getReceivedCollaborationRequestsUseCase: GetReceivedCollaborationRequestsUseCase,
    private val acceptCollaborationRequestUseCase: AcceptCollaborationRequestUseCase,
    private val rejectCollaborationRequestUseCase: RejectCollaborationRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollaborationRequestsState>(CollaborationRequestsState.Loading)
    val uiState: StateFlow<CollaborationRequestsState> = _uiState.asStateFlow()

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.value = CollaborationRequestsState.Loading
            getReceivedCollaborationRequestsUseCase()
                .onSuccess { requests ->
                    if (requests.isEmpty()) {
                        _uiState.value = CollaborationRequestsState.Empty
                    } else {
                        _uiState.value = CollaborationRequestsState.Success(requests)
                    }
                }
                .onFailure { error ->
                    _uiState.value = CollaborationRequestsState.Error(error.message ?: "Failed to load requests")
                }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            acceptCollaborationRequestUseCase(requestId)
                .onSuccess {
                    loadRequests() // Refresh
                }
                .onFailure {
                    // Handle error
                }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            rejectCollaborationRequestUseCase(requestId)
                .onSuccess {
                    loadRequests() // Refresh
                }
                .onFailure {
                    // Handle error
                }
        }
    }
}
