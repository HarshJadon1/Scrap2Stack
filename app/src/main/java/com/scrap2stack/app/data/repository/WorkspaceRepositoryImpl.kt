package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.ProjectMemberDto
import com.scrap2stack.app.data.remote.dto.RoadmapItemDto
import com.scrap2stack.app.data.remote.dto.TaskDto
import com.scrap2stack.app.domain.repository.WorkspaceRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class WorkspaceRepositoryImpl : WorkspaceRepository {

    override suspend fun getTasks(projectId: String): Result<List<TaskDto>> = withContext(Dispatchers.IO) {
        try {
            val tasks = supabase.from("tasks")
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<TaskDto>()

            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTask(task: TaskDto): Result<TaskDto> = withContext(Dispatchers.IO) {
        try {
            val created = supabase.from("tasks")
                .insert(task) {
                    select()
                }
                .decodeSingle<TaskDto>()

            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("tasks")
                .update(buildJsonObject { put("status", status) }) {
                    filter {
                        eq("id", taskId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRoadmapItems(projectId: String): Result<List<RoadmapItemDto>> = withContext(Dispatchers.IO) {
        try {
            val items = supabase.from("roadmap_items")
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<RoadmapItemDto>()

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRoadmapItemStatus(itemId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.from("roadmap_items")
                .update(buildJsonObject { put("status", status) }) {
                    filter {
                        eq("id", itemId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectMembers(projectId: String): Result<List<ProjectMemberDto>> = withContext(Dispatchers.IO) {
        try {
            val members = supabase.from("project_members")
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<ProjectMemberDto>()

            Result.success(members)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
