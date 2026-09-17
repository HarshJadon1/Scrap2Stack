package com.scrap2stack.app.feature.charms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.domain.repository.CharmsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CharmsUiState {
    object Loading : CharmsUiState()
    data class Success(
        val totalCharms: Int,
        val history: List<CharmContribution>
    ) : CharmsUiState()
    data class Error(val message: String) : CharmsUiState()
}

class CharmsViewModel(
    private val charmsRepository: CharmsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharmsUiState>(CharmsUiState.Loading)
    val uiState: StateFlow<CharmsUiState> = _uiState.asStateFlow()

    init {
        loadCharmsData()
    }

    fun loadCharmsData() {
        viewModelScope.launch {
            _uiState.value = CharmsUiState.Loading
            try {
                coroutineScope {
                    val charmsDeferred = async { charmsRepository.getUserCharms() }
                    val historyDeferred = async { charmsRepository.getContributionHistory() }

                    val charmsResult = charmsDeferred.await()
                    val historyResult = historyDeferred.await()

                    _uiState.value = CharmsUiState.Success(
                        totalCharms = charmsResult.getOrDefault(0),
                        history = historyResult.getOrDefault(emptyList())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = CharmsUiState.Error("Failed to load charms: ${e.localizedMessage}")
            }
        }
    }
}
