package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyProjectsData(
    val created: List<Project> = emptyList(),
    val joined: List<Project> = emptyList(),
    val saved: List<Project> = emptyList(),
    val completed: List<Project> = emptyList()
)

sealed class MyProjectsUiState {
    object Loading : MyProjectsUiState()
    data class Success(val data: MyProjectsData) : MyProjectsUiState()
    data class Error(val message: String) : MyProjectsUiState()
}

class MyProjectsViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyProjectsUiState>(MyProjectsUiState.Loading)
    val uiState: StateFlow<MyProjectsUiState> = _uiState.asStateFlow()

    init {
        loadMyProjects()
    }

    fun loadMyProjects() {
        viewModelScope.launch {
            _uiState.value = MyProjectsUiState.Loading
            try {
                // Execute all project categories concurrently in parallel
                coroutineScope {
                    val createdDeferred = async { repository.getMyProjects() }
                    val joinedDeferred = async { repository.getJoinedProjects() }
                    val savedDeferred = async { repository.getSavedProjects() }
                    val completedDeferred = async { repository.getCompletedProjects() }

                    val createdResult = createdDeferred.await()
                    val joinedResult = joinedDeferred.await()
                    val savedResult = savedDeferred.await()
                    val completedResult = completedDeferred.await()

                    _uiState.value = MyProjectsUiState.Success(
                        MyProjectsData(
                            created = createdResult.getOrDefault(emptyList()),
                            joined = joinedResult.getOrDefault(emptyList()),
                            saved = savedResult.getOrDefault(emptyList()),
                            completed = completedResult.getOrDefault(emptyList())
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = MyProjectsUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
