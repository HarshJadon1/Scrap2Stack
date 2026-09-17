package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.ProjectMember
import com.scrap2stack.app.domain.model.RoadmapItem
import com.scrap2stack.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
    suspend fun getTasks(projectId: String): Result<List<Task>>
    suspend fun createTask(task: Task, projectId: String): Result<Task>
    suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit>
    suspend fun getRoadmapItems(projectId: String): Result<List<RoadmapItem>>
    suspend fun updateRoadmapItemStatus(itemId: String, status: String): Result<Unit>
    suspend fun getProjectMembers(projectId: String): Result<List<ProjectMember>>
    fun observeWorkspaceUpdates(projectId: String): Flow<Unit>
}
