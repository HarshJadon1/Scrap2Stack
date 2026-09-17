package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.remote.dto.ProjectMemberDto
import com.scrap2stack.app.data.remote.dto.RoadmapItemDto
import com.scrap2stack.app.data.remote.dto.TaskDto
import com.scrap2stack.app.data.remote.dto.UserDto
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.domain.repository.WorkspaceRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class WorkspaceRepositoryImpl : WorkspaceRepository {

    override suspend fun getTasks(projectId: String): Result<List<Task>> = withContext(Dispatchers.IO) {
        try {
            val tasks = supabase.from("tasks")
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<TaskDto>()

            Result.success(tasks.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTask(task: Task, projectId: String): Result<Task> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""
            val taskPayload = buildJsonObject {
                put("project_id", projectId)
                put("title", task.title)
                if (!task.assignee.isNullOrBlank()) put("assignee_id", task.assignee)
                put("priority", task.priority.name)
                put("status", task.status.name)
                if (task.skill.isNotBlank()) put("skill", task.skill)
                if (!task.dueDate.isNullOrBlank()) put("due_date", task.dueDate)
                if (currentUserId.isNotBlank()) put("created_by", currentUserId)
            }

            val created = supabase.from("tasks")
                .insert(taskPayload) {
                    select()
                }
                .decodeSingle<TaskDto>()

            Result.success(created.toDomain())
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

    override suspend fun getRoadmapItems(projectId: String): Result<List<RoadmapItem>> = withContext(Dispatchers.IO) {
        try {
            val items = supabase.from("roadmap_items")
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<RoadmapItemDto>()

            Result.success(items.map { it.toDomain() })
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

    override suspend fun getProjectMembers(projectId: String): Result<List<ProjectMember>> = withContext(Dispatchers.IO) {
        try {
            val members = supabase.from("project_members")
                .select {
                    filter {
                        eq("project_id", projectId)
                    }
                }
                .decodeList<ProjectMemberDto>()

            Result.success(members.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeWorkspaceUpdates(projectId: String): Flow<Unit> = callbackFlow {
        val channelTopic = "workspace_${projectId.take(8)}_${UUID.randomUUID().toString().take(6)}"
        val channel = supabase.realtime.channel(channelTopic)
        
        val tasksFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "tasks"
        }
        val roadmapFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "roadmap_items"
        }
        val membersFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "project_members"
        }

        try {
            channel.subscribe()
        } catch (ignored: Exception) {}

        val job1 = launch { tasksFlow.collect { trySend(Unit) } }
        val job2 = launch { roadmapFlow.collect { trySend(Unit) } }
        val job3 = launch { membersFlow.collect { trySend(Unit) } }

        awaitClose {
            job1.cancel()
            job2.cancel()
            job3.cancel()
            launch {
                try {
                    supabase.realtime.removeChannel(channel)
                } catch (ignored: Exception) {}
            }
        }
    }

    private fun TaskDto.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            assignee = assigneeId,
            priority = try { TaskPriority.valueOf(priority.uppercase()) } catch (e: Exception) { TaskPriority.MEDIUM },
            skill = skill,
            status = try { TaskStatus.valueOf(status.uppercase()) } catch (e: Exception) { TaskStatus.TODO },
            dueDate = dueDate
        )
    }

    private fun RoadmapItemDto.toDomain(): RoadmapItem {
        return RoadmapItem(
            id = id,
            roadmapId = roadmapId,
            title = title,
            description = description,
            order = order,
            status = status,
            requiredSkills = requiredSkills,
            requiredRoles = requiredRoles,
            estimatedEffort = estimatedEffort,
            dependencies = dependencies,
            milestone = milestone,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun ProjectMemberDto.toDomain(): ProjectMember {
        return ProjectMember(
            id = id,
            projectId = projectId,
            userId = userId,
            role = try { ProjectRole.valueOf(role.uppercase()) } catch (e: Exception) { ProjectRole.CONTRIBUTOR },
            joinedAt = joinedAt,
            user = profiles?.toDomain()
        )
    }

    private fun UserDto.toDomain(): Developer {
        return Developer(
            id = id,
            name = name,
            username = username,
            bio = bio ?: "",
            profileImageUrl = profileImage,
            skills = skills,
            interests = interests,
            experienceLevel = try {
                ExperienceLevel.valueOf(experienceLevel?.uppercase() ?: "BEGINNER")
            } catch (e: Exception) {
                ExperienceLevel.BEGINNER
            },
            githubUrl = githubUrl ?: "",
            linkedinUrl = linkedinUrl ?: "",
            portfolioUrl = portfolioUrl ?: "",
            charms = charms,
            createdAt = createdAt ?: "",
            updatedAt = updatedAt ?: ""
        )
    }
}
