package com.scrap2stack.app.feature.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.domain.model.ProjectMember
import com.scrap2stack.app.domain.model.RoadmapItem
import com.scrap2stack.app.domain.model.Task
import com.scrap2stack.app.domain.model.TaskPriority
import com.scrap2stack.app.domain.model.TaskStatus
import com.scrap2stack.app.domain.repository.WorkspaceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WorkspaceState {
    object Loading : WorkspaceState()
    data class Success(
        val tasks: List<Task>,
        val roadmapItems: List<RoadmapItem>,
        val members: List<ProjectMember>
    ) : WorkspaceState()
    data class Error(val message: String) : WorkspaceState()
}

class WorkspaceViewModel(
    private val repository: WorkspaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceState>(WorkspaceState.Loading)
    val uiState: StateFlow<WorkspaceState> = _uiState.asStateFlow()

    fun loadWorkspaceData(projectId: String) {
        viewModelScope.launch {
            _uiState.value = WorkspaceState.Loading
            try {
                // Fetch tasks, roadmap items, and project members concurrently in parallel
                coroutineScope {
                    val tasksDeferred = async { repository.getTasks(projectId) }
                    val roadmapDeferred = async { repository.getRoadmapItems(projectId) }
                    val membersDeferred = async { repository.getProjectMembers(projectId) }

                    val tasksResult = tasksDeferred.await()
                    val roadmapResult = roadmapDeferred.await()
                    val membersResult = membersDeferred.await()

                    _uiState.value = WorkspaceState.Success(
                        tasks = tasksResult.getOrDefault(emptyList()),
                        roadmapItems = roadmapResult.getOrDefault(emptyList()),
                        members = membersResult.getOrDefault(emptyList())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = WorkspaceState.Error("Failed to load workspace: ${e.localizedMessage}")
            }
        }
    }

    fun subscribeToRealtimeWorkspace(projectId: String) {
        viewModelScope.launch {
            repository.observeWorkspaceUpdates(projectId).collect {
                loadWorkspaceData(projectId)
            }
        }
    }

    fun createNewTask(title: String, skill: String, priority: TaskPriority, projectId: String) {
        viewModelScope.launch {
            val newTask = Task(
                id = "",
                title = title,
                assignee = null,
                priority = priority,
                skill = skill,
                status = TaskStatus.TODO
            )
            repository.createTask(newTask, projectId)
                .onSuccess {
                    loadWorkspaceData(projectId)
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
