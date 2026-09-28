package com.scrap2stack.app.data.local.cache

import com.scrap2stack.app.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe, reactive in-memory caching layer for Scrap2Stack.
 * Provides instant zero-latency rendering, offline resilience,
 * and optimistic UI updates for projects, tasks, roadmaps, members, and notifications.
 */
object AppCache {

    // ==========================================
    // PROJECTS CACHE
    // ==========================================
    private val projectsMap = ConcurrentHashMap<String, Project>()
    private val _projectsFlow = MutableStateFlow<List<Project>>(emptyList())
    val projectsFlow: StateFlow<List<Project>> = _projectsFlow.asStateFlow()

    private val _myProjectsFlow = MutableStateFlow<List<Project>>(emptyList())
    val myProjectsFlow: StateFlow<List<Project>> = _myProjectsFlow.asStateFlow()

    fun saveProjects(projects: List<Project>) {
        projects.forEach { projectsMap[it.id] = it }
        _projectsFlow.value = projects
    }

    fun saveMyProjects(projects: List<Project>) {
        projects.forEach { projectsMap[it.id] = it }
        _myProjectsFlow.value = projects
    }

    fun getProject(projectId: String): Project? = projectsMap[projectId]

    fun getAllProjects(): List<Project> = _projectsFlow.value.ifEmpty { projectsMap.values.toList() }

    fun putProject(project: Project) {
        projectsMap[project.id] = project
        _projectsFlow.value = _projectsFlow.value.toMutableList().apply {
            val idx = indexOfFirst { it.id == project.id }
            if (idx >= 0) set(idx, project) else add(0, project)
        }
        _myProjectsFlow.value = _myProjectsFlow.value.toMutableList().apply {
            val idx = indexOfFirst { it.id == project.id }
            if (idx >= 0) set(idx, project)
        }
    }

    fun removeProject(projectId: String) {
        projectsMap.remove(projectId)
        tasksMap.remove(projectId)
        roadmapMap.remove(projectId)
        membersMap.remove(projectId)
        _projectsFlow.value = _projectsFlow.value.filter { it.id != projectId }
        _myProjectsFlow.value = _myProjectsFlow.value.filter { it.id != projectId }
    }

    // ==========================================
    // TASKS CACHE
    // ==========================================
    private val tasksMap = ConcurrentHashMap<String, List<Task>>()
    private val tasksFlowMap = ConcurrentHashMap<String, MutableStateFlow<List<Task>>>()

    fun saveTasks(projectId: String, tasks: List<Task>) {
        tasksMap[projectId] = tasks
        getOrCreateTasksFlow(projectId).value = tasks
    }

    fun getTasks(projectId: String): List<Task>? = tasksMap[projectId]

    fun getTasksFlow(projectId: String): StateFlow<List<Task>> = getOrCreateTasksFlow(projectId).asStateFlow()

    private fun getOrCreateTasksFlow(projectId: String): MutableStateFlow<List<Task>> {
        return tasksFlowMap.getOrPut(projectId) {
            MutableStateFlow(tasksMap[projectId] ?: emptyList())
        }
    }

    fun updateTask(projectId: String, updatedTask: Task) {
        val current = tasksMap[projectId] ?: emptyList()
        val next = current.toMutableList().apply {
            val idx = indexOfFirst { it.id == updatedTask.id }
            if (idx >= 0) set(idx, updatedTask) else add(updatedTask)
        }
        tasksMap[projectId] = next
        getOrCreateTasksFlow(projectId).value = next
    }

    fun updateTaskStatus(projectId: String, taskId: String, newStatus: TaskStatus) {
        val current = tasksMap[projectId] ?: emptyList()
        val next = current.map { task ->
            if (task.id == taskId) task.copy(status = newStatus) else task
        }
        tasksMap[projectId] = next
        getOrCreateTasksFlow(projectId).value = next
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        tasksMap.forEach { (projectId, tasks) ->
            if (tasks.any { it.id == taskId }) {
                val updated = tasks.map { if (it.id == taskId) it.copy(status = newStatus) else it }
                tasksMap[projectId] = updated
                getOrCreateTasksFlow(projectId).value = updated
            }
        }
    }

    fun removeTask(projectId: String, taskId: String) {
        val current = tasksMap[projectId] ?: emptyList()
        val next = current.filter { it.id != taskId }
        tasksMap[projectId] = next
        getOrCreateTasksFlow(projectId).value = next
    }

    // ==========================================
    // ROADMAP CACHE
    // ==========================================
    private val roadmapMap = ConcurrentHashMap<String, List<RoadmapItem>>()
    private val roadmapFlowMap = ConcurrentHashMap<String, MutableStateFlow<List<RoadmapItem>>>()

    fun saveRoadmap(projectId: String, items: List<RoadmapItem>) {
        roadmapMap[projectId] = items
        getOrCreateRoadmapFlow(projectId).value = items
    }

    fun getRoadmap(projectId: String): List<RoadmapItem>? = roadmapMap[projectId]

    fun getRoadmapFlow(projectId: String): StateFlow<List<RoadmapItem>> = getOrCreateRoadmapFlow(projectId).asStateFlow()

    private fun getOrCreateRoadmapFlow(projectId: String): MutableStateFlow<List<RoadmapItem>> {
        return roadmapFlowMap.getOrPut(projectId) {
            MutableStateFlow(roadmapMap[projectId] ?: emptyList())
        }
    }

    fun updateRoadmapItemStatus(projectId: String, itemId: String, newStatus: String) {
        val current = roadmapMap[projectId] ?: emptyList()
        val next = current.map { item ->
            if (item.id == itemId) item.copy(status = newStatus) else item
        }
        roadmapMap[projectId] = next
        getOrCreateRoadmapFlow(projectId).value = next
    }

    // ==========================================
    // PROJECT MEMBERS CACHE
    // ==========================================
    private val membersMap = ConcurrentHashMap<String, List<ProjectMember>>()

    fun saveMembers(projectId: String, members: List<ProjectMember>) {
        membersMap[projectId] = members
    }

    fun getMembers(projectId: String): List<ProjectMember>? = membersMap[projectId]

    // ==========================================
    // USER / DEVELOPER CACHE
    // ==========================================
    private val usersMap = ConcurrentHashMap<String, Developer>()

    fun saveUser(user: Developer) {
        usersMap[user.id] = user
    }

    fun getUser(userId: String): Developer? = usersMap[userId]

    // ==========================================
    // NOTIFICATIONS CACHE
    // ==========================================
    private val _notificationsFlow = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notificationsFlow: StateFlow<List<NotificationItem>> = _notificationsFlow.asStateFlow()

    private val _unreadCountFlow = MutableStateFlow(0)
    val unreadCountFlow: StateFlow<Int> = _unreadCountFlow.asStateFlow()

    fun saveNotifications(notifications: List<NotificationItem>) {
        _notificationsFlow.value = notifications
        _unreadCountFlow.value = notifications.count { !it.read }
    }

    fun getNotifications(): List<NotificationItem> = _notificationsFlow.value

    fun markNotificationAsRead(id: String) {
        val updated = _notificationsFlow.value.map {
            if (it.id == id) it.copy(read = true) else it
        }
        _notificationsFlow.value = updated
        _unreadCountFlow.value = updated.count { !it.read }
    }

    // ==========================================
    // CLEAR / INVALIDATE CACHE
    // ==========================================
    fun clearAll() {
        projectsMap.clear()
        _projectsFlow.value = emptyList()
        _myProjectsFlow.value = emptyList()
        tasksMap.clear()
        tasksFlowMap.clear()
        roadmapMap.clear()
        roadmapFlowMap.clear()
        membersMap.clear()
        usersMap.clear()
        _notificationsFlow.value = emptyList()
        _unreadCountFlow.value = 0
    }
}
