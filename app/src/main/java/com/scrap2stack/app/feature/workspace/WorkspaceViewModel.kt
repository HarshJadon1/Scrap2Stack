package com.scrap2stack.app.feature.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrap2stack.app.core.network.GeminiAiService
import com.scrap2stack.app.data.local.cache.AppCache
import com.scrap2stack.app.data.repository.CharmsRepositoryImpl
import com.scrap2stack.app.data.repository.ProjectRepositoryImpl
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.domain.repository.CharmsRepository
import com.scrap2stack.app.domain.repository.ProjectRepository
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
        val project: Project? = null,
        val tasks: List<Task>,
        val roadmapItems: List<RoadmapItem>,
        val members: List<ProjectMember>,
        val isShipped: Boolean = false
    ) : WorkspaceState()
    data class Error(val message: String) : WorkspaceState()
}

class WorkspaceViewModel(
    private val repository: WorkspaceRepository,
    private val projectRepository: ProjectRepository = ProjectRepositoryImpl(),
    private val charmsRepository: CharmsRepository = CharmsRepositoryImpl()
) : ViewModel() {

    private val geminiService = GeminiAiService()

    private val _isGeneratingRoadmap = MutableStateFlow(false)
    val isGeneratingRoadmap: StateFlow<Boolean> = _isGeneratingRoadmap.asStateFlow()

    private val _uiState = MutableStateFlow<WorkspaceState>(WorkspaceState.Loading)
    val uiState: StateFlow<WorkspaceState> = _uiState.asStateFlow()

    fun loadWorkspaceData(projectId: String) {
        viewModelScope.launch {
            _uiState.value = WorkspaceState.Loading
            try {
                // Fetch project metadata, tasks, roadmap items, and project members concurrently in parallel
                coroutineScope {
                    val projectDeferred = async { projectRepository.getProjectById(projectId) }
                    val tasksDeferred = async { repository.getTasks(projectId) }
                    val roadmapDeferred = async { repository.getRoadmapItems(projectId) }
                    val membersDeferred = async { repository.getProjectMembers(projectId) }

                    val projectResult = projectDeferred.await()
                    val tasksResult = tasksDeferred.await()
                    val roadmapResult = roadmapDeferred.await()
                    val membersResult = membersDeferred.await()

                    val project = projectResult.getOrNull()
                    val isShipped = project?.status == ProjectStatus.COMPLETED

                    _uiState.value = WorkspaceState.Success(
                        project = project,
                        tasks = tasksResult.getOrDefault(emptyList()),
                        roadmapItems = roadmapResult.getOrDefault(emptyList()),
                        members = membersResult.getOrDefault(emptyList()),
                        isShipped = isShipped
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
            // Check if moving to COMPLETED to award charms
            val isCompleting = status.uppercase() == "COMPLETED"
            repository.updateTaskStatus(taskId, status)
                .onSuccess {
                    if (isCompleting) {
                        try {
                            charmsRepository.awardTaskCompletionCharms(taskId, projectId)
                        } catch (ignored: Exception) {}
                    }
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

    fun generateAiRoadmap(
        projectId: String,
        projectName: String? = null,
        problem: String? = null,
        techStack: List<String>? = null
    ) {
        viewModelScope.launch {
            _isGeneratingRoadmap.value = true
            val currentProject = (_uiState.value as? WorkspaceState.Success)?.project
            val targetName = projectName ?: currentProject?.name ?: "Scrap2Stack Target"
            val targetProblem = problem ?: currentProject?.problem?.ifBlank { null } ?: "Project revival and tech debt resolution"
            val targetTech = techStack ?: currentProject?.technologies?.filter { it.isNotBlank() }?.ifEmpty { null } ?: listOf("Kotlin", "Android", "Supabase")

            val result = geminiService.generateRevivalRoadmap(
                projectName = targetName,
                techStack = targetTech,
                problem = targetProblem
            )
            result.onSuccess { generatedItems ->
                val currentState = _uiState.value
                if (currentState is WorkspaceState.Success) {
                    _uiState.value = currentState.copy(
                        roadmapItems = generatedItems
                    )
                } else {
                    _uiState.value = WorkspaceState.Success(
                        project = currentProject,
                        tasks = emptyList(),
                        roadmapItems = generatedItems,
                        members = emptyList()
                    )
                }
            }
            _isGeneratingRoadmap.value = false
        }
    }

    fun convertRoadmapPhaseToTasks(item: RoadmapItem, projectId: String) {
        viewModelScope.launch {
            val task1 = Task(
                id = java.util.UUID.randomUUID().toString(),
                title = "[Phase ${item.order}] Core Implementation: ${item.title}",
                assignee = null,
                priority = TaskPriority.HIGH,
                skill = item.requiredSkills.firstOrNull() ?: "Engineering",
                status = TaskStatus.TODO
            )
            val task2 = Task(
                id = java.util.UUID.randomUUID().toString(),
                title = "[Phase ${item.order}] QA & Test Suite for ${item.title}",
                assignee = null,
                priority = TaskPriority.MEDIUM,
                skill = item.requiredSkills.getOrNull(1) ?: "Testing",
                status = TaskStatus.TODO
            )

            repository.createTask(task1, projectId)
            repository.createTask(task2, projectId)

            val currentState = _uiState.value
            if (currentState is WorkspaceState.Success) {
                _uiState.value = currentState.copy(
                    tasks = currentState.tasks + listOf(task1, task2)
                )
            }
        }
    }

    /**
     * Completes the project revival and publishes it to the Community Shipped Stacks showcase.
     * Awards +100 Charms to team members and updates reactive cache.
     */
    fun shipProject(projectId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val currentState = _uiState.value as? WorkspaceState.Success
            val currentProject = currentState?.project
            if (currentProject != null) {
                val updatedProject = currentProject.copy(status = ProjectStatus.COMPLETED)
                projectRepository.updateProject(updatedProject)
                try {
                    charmsRepository.awardProjectShippedCharms(projectId)
                } catch (ignored: Exception) {}
                AppCache.putProject(updatedProject)
                _uiState.value = currentState.copy(
                    project = updatedProject,
                    isShipped = true
                )
                onSuccess()
            }
        }
    }
}
