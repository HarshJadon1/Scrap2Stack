package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.usecase.*

class CollaborationViewModelFactory(
    private val sendCollaborationRequestUseCase: SendCollaborationRequestUseCase,
    private val getReceivedRequestsUseCase: GetReceivedCollaborationRequestsUseCase,
    private val getSentRequestsUseCase: GetSentCollaborationRequestsUseCase,
    private val acceptRequestUseCase: AcceptCollaborationRequestUseCase,
    private val rejectRequestUseCase: RejectCollaborationRequestUseCase,
    private val cancelRequestUseCase: CancelCollaborationRequestUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollaborationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CollaborationViewModel(
                sendCollaborationRequestUseCase,
                getReceivedRequestsUseCase,
                getSentRequestsUseCase,
                acceptRequestUseCase,
                rejectRequestUseCase,
                cancelRequestUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
