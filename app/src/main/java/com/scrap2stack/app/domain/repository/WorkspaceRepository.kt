package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.data.remote.dto.ProjectMemberDto
import com.scrap2stack.app.data.remote.dto.RoadmapItemDto
import com.scrap2stack.app.data.remote.dto.TaskDto

interface WorkspaceRepository {
    suspend fun getTasks(projectId: String): Result<List<TaskDto>>
    suspend fun createTask(task: TaskDto): Result<TaskDto>
    suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit>
    suspend fun getRoadmapItems(projectId: String): Result<List<RoadmapItemDto>>
    suspend fun updateRoadmapItemStatus(itemId: String, status: String): Result<Unit>
    suspend fun getProjectMembers(projectId: String): Result<List<ProjectMemberDto>>
}
