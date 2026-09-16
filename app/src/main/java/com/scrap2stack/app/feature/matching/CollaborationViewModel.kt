package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.CollaborationRequest
import com.scrap2stack.app.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CollaborationUiState {
    object Idle : CollaborationUiState()
    object Loading : CollaborationUiState()
    data class Success(val message: String) : CollaborationUiState()
    data class Error(val message: String) : CollaborationUiState()
}

sealed class RequestsUiState {
    object Loading : RequestsUiState()
    data class Success(val received: List<CollaborationRequest>, val sent: List<CollaborationRequest>) : RequestsUiState()
    data class Error(val message: String) : RequestsUiState()
}

class CollaborationViewModel(
    private val sendCollaborationRequestUseCase: SendCollaborationRequestUseCase,
    private val getReceivedRequestsUseCase: GetReceivedCollaborationRequestsUseCase,
    private val getSentRequestsUseCase: GetSentCollaborationRequestsUseCase,
    private val acceptRequestUseCase: AcceptCollaborationRequestUseCase,
    private val rejectRequestUseCase: RejectCollaborationRequestUseCase,
    private val cancelRequestUseCase: CancelCollaborationRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollaborationUiState>(CollaborationUiState.Idle)
    val uiState: StateFlow<CollaborationUiState> = _uiState.asStateFlow()

    private val _requestsState = MutableStateFlow<RequestsUiState>(RequestsUiState.Loading)
    val requestsState: StateFlow<RequestsUiState> = _requestsState.asStateFlow()

    fun sendRequest(projectId: String, receiverId: String, message: String) {
        viewModelScope.launch {
            _uiState.value = CollaborationUiState.Loading
            sendCollaborationRequestUseCase(projectId, receiverId, message)
                .onSuccess {
                    _uiState.value = CollaborationUiState.Success("Invitation sent successfully")
                }
                .onFailure { error ->
                    _uiState.value = CollaborationUiState.Error(error.message ?: "Failed to send invitation")
                }
        }
    }

    fun loadRequests() {
        viewModelScope.launch {
            _requestsState.value = RequestsUiState.Loading
            val receivedResult = getReceivedRequestsUseCase()
            val sentResult = getSentRequestsUseCase()

            if (receivedResult.isSuccess && sentResult.isSuccess) {
                _requestsState.value = RequestsUiState.Success(
                    received = receivedResult.getOrNull() ?: emptyList(),
                    sent = sentResult.getOrNull() ?: emptyList()
                )
            } else {
                _requestsState.value = RequestsUiState.Error("Failed to load collaboration requests")
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = CollaborationUiState.Loading
            acceptRequestUseCase(requestId)
                .onSuccess {
                    _uiState.value = CollaborationUiState.Success("Accepted successfully")
                    loadRequests()
                }
                .onFailure { error ->
                    _uiState.value = CollaborationUiState.Error(error.message ?: "Failed to accept")
                }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = CollaborationUiState.Loading
            rejectRequestUseCase(requestId)
                .onSuccess {
                    _uiState.value = CollaborationUiState.Success("Rejected successfully")
                    loadRequests()
                }
                .onFailure { error ->
                    _uiState.value = CollaborationUiState.Error(error.message ?: "Failed to reject")
                }
        }
    }

    fun cancelRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = CollaborationUiState.Loading
            cancelRequestUseCase(requestId)
                .onSuccess {
                    _uiState.value = CollaborationUiState.Success("Cancelled successfully")
                    loadRequests()
                }
                .onFailure { error ->
                    _uiState.value = CollaborationUiState.Error(error.message ?: "Failed to cancel")
                }
        }
    }

    fun resetState() {
        _uiState.value = CollaborationUiState.Idle
    }
}
