package com.scrap2stack.app.feature.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspaceState {
    object Loading : WorkspaceState()
    data class Success(
        val workspace: WorkspaceDto,
        val tasks: List<TaskDto>,
        val roadmap: RoadmapResponse?,
        val contributions: List<GitHubContributionDto>
    ) : WorkspaceState()
    data class Error(val message: String) : WorkspaceState()
}

class WorkspaceViewModel(
    private val repository: ProjectRepository = ProjectRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceState>(WorkspaceState.Loading)
    val uiState: StateFlow<WorkspaceState> = _uiState.asStateFlow()

    fun loadWorkspaceData(projectId: String) {
        viewModelScope.launch {
            _uiState.value = WorkspaceState.Loading
            try {
                val wsResponse = repository.getWorkspace(projectId)
                val tasksResponse = repository.getProjectTasks(projectId)
                val roadmapResponse = repository.getRoadmap(projectId)
                // For contributions, we usually fetch user's contributions or project specific ones
                // Here we fetch my contributions for the timeline
                val contribResponse = repository.getMyContributions()

                if (wsResponse.isSuccessful && wsResponse.body()?.success == true) {
                    _uiState.value = WorkspaceState.Success(
                        workspace = wsResponse.body()!!.data!!,
                        tasks = tasksResponse.body()?.data ?: emptyList(),
                        roadmap = roadmapResponse.body()?.data,
                        contributions = contribResponse.body()?.data ?: emptyList()
                    )
                } else {
                    _uiState.value = WorkspaceState.Error(wsResponse.body()?.message ?: "Failed to load workspace")
                }
            } catch (e: Exception) {
                _uiState.value = WorkspaceState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: String, projectId: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateTaskStatus(taskId, status)
                if (response.isSuccessful) {
                    loadWorkspaceData(projectId)
                }
            } catch (e: Exception) { }
        }
    }

    fun syncGitHub(projectId: String) {
        viewModelScope.launch {
            try {
                repository.syncGitHub(projectId)
                loadWorkspaceData(projectId)
            } catch (e: Exception) { }
        }
    }
}
