package com.scrap2stack.app.feature.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyProjectsData(
    val created: List<Project> = emptyList(),
    val joined: List<Project> = emptyList(),
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
                val createdResult = repository.getMyProjects()
                val joinedResult = repository.getJoinedProjects()
                val completedResult = repository.getCompletedProjects()

                if (createdResult.isSuccess && joinedResult.isSuccess && completedResult.isSuccess) {
                    _uiState.value = MyProjectsUiState.Success(
                        MyProjectsData(
                            created = createdResult.getOrDefault(emptyList()),
                            joined = joinedResult.getOrDefault(emptyList()),
                            completed = completedResult.getOrDefault(emptyList())
                        )
                    )
                } else {
                    val errorMsg = createdResult.exceptionOrNull()?.message
                        ?: joinedResult.exceptionOrNull()?.message
                        ?: completedResult.exceptionOrNull()?.message
                        ?: "Failed to load projects"
                    _uiState.value = MyProjectsUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = MyProjectsUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
