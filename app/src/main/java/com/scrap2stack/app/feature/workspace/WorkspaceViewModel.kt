package com.scrap2stack.app.feature.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.data.remote.dto.ProjectMemberDto
import com.scrap2stack.app.data.remote.dto.RoadmapItemDto
import com.scrap2stack.app.data.remote.dto.TaskDto
import com.scrap2stack.app.data.repository.WorkspaceRepositoryImpl
import com.scrap2stack.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspaceState {
    object Loading : WorkspaceState()
    data class Success(
        val tasks: List<TaskDto>,
        val roadmapItems: List<RoadmapItemDto>,
        val members: List<ProjectMemberDto>
    ) : WorkspaceState()
    data class Error(val message: String) : WorkspaceState()
}

class WorkspaceViewModel(
    private val repository: WorkspaceRepository = WorkspaceRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceState>(WorkspaceState.Loading)
    val uiState: StateFlow<WorkspaceState> = _uiState.asStateFlow()

    fun loadWorkspaceData(projectId: String) {
        viewModelScope.launch {
            _uiState.value = WorkspaceState.Loading
            try {
                val tasksResult = repository.getTasks(projectId)
                val roadmapResult = repository.getRoadmapItems(projectId)
                val membersResult = repository.getProjectMembers(projectId)

                _uiState.value = WorkspaceState.Success(
                    tasks = tasksResult.getOrDefault(emptyList()),
                    roadmapItems = roadmapResult.getOrDefault(emptyList()),
                    members = membersResult.getOrDefault(emptyList())
                )
            } catch (e: Exception) {
                _uiState.value = WorkspaceState.Error("Failed to load workspace: ${e.localizedMessage}")
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: String, projectId: String) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, status)
                .onSuccess {
                    loadWorkspaceData(projectId)
                }
        }
    }

    fun updateRoadmapStatus(itemId: String, status: String, projectId: String) {
        viewModelScope.launch {
            repository.updateRoadmapItemStatus(itemId, status)
                .onSuccess {
                    loadWorkspaceData(projectId)
                }
        }
    }
}
