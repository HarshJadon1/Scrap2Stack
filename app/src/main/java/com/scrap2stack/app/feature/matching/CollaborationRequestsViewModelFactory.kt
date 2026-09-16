package com.scrap2stack.app.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.scrap2stack.app.domain.usecase.AcceptCollaborationRequestUseCase
import com.scrap2stack.app.domain.usecase.GetReceivedCollaborationRequestsUseCase
import com.scrap2stack.app.domain.usecase.RejectCollaborationRequestUseCase

class CollaborationRequestsViewModelFactory(
    private val getReceivedCollaborationRequestsUseCase: GetReceivedCollaborationRequestsUseCase,
    private val acceptCollaborationRequestUseCase: AcceptCollaborationRequestUseCase,
    private val rejectCollaborationRequestUseCase: RejectCollaborationRequestUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollaborationRequestsViewModel::class.java)) {
            return CollaborationRequestsViewModel(
                getReceivedCollaborationRequestsUseCase,
                acceptCollaborationRequestUseCase,
                rejectCollaborationRequestUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
