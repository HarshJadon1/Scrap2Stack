package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.supabase
import com.scrap2stack.app.data.local.cache.AppCache
import com.scrap2stack.app.data.mapper.*
import com.scrap2stack.app.data.remote.dto.*
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.service.ScrapAIEngine
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectRepositoryImpl : ProjectRepository {

    override suspend fun getDiscoverProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val dtos = supabase.from("projects")
                .select()
                .decodeList<ProjectDto>()
            val projects = dtos.map { it.toDomain() }
            AppCache.saveProjects(projects)
            Result.success(projects)
        } catch (e: Exception) {
            val cached = AppCache.getAllProjects()
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getFeaturedProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val dtos = supabase.from("projects")
                .select {
                    order("revival_score", Order.DESCENDING)
                    limit(5)
                }
                .decodeList<ProjectDto>()
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val dtos = supabase.from("projects")
                .select {
                    order("created_at", Order.DESCENDING)
                    limit(5)
                }
                .decodeList<ProjectDto>()
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            val dtos = supabase.from("projects")
                .select {
                    filter {
                        eq("owner_id", userId)
                    }
                }
                .decodeList<ProjectDto>()
            val projects = dtos.map { it.toDomain() }
            AppCache.saveMyProjects(projects)
            Result.success(projects)
        } catch (e: Exception) {
            val cached = AppCache.myProjectsFlow.value
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getJoinedProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val memberDtos = supabase.from("project_members")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ProjectMemberDto>()

            val projectIds = memberDtos.map { it.projectId }.distinct()
            if (projectIds.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val dtos = supabase.from("projects")
                .select {
                    filter {
                        isIn("id", projectIds)
                        neq("owner_id", userId)
                    }
                }
                .decodeList<ProjectDto>()

            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCompletedProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val ownedCompletedDtos = supabase.from("projects")
                .select {
                    filter {
                        eq("owner_id", userId)
                        ilike("status", "completed")
                    }
                }
                .decodeList<ProjectDto>()

            val memberDtos = supabase.from("project_members")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ProjectMemberDto>()

            val projectIds = memberDtos.map { it.projectId }.distinct()
            val joinedCompletedDtos = if (projectIds.isNotEmpty()) {
                supabase.from("projects")
                    .select {
                        filter {
                            isIn("id", projectIds)
                            ilike("status", "completed")
                        }
                    }
                    .decodeList<ProjectDto>()
            } else {
                emptyList()
            }

            val allCompleted = (ownedCompletedDtos + joinedCompletedDtos).distinctBy { it.id }
            Result.success(allCompleted.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSavedProjects(): Result<List<Project>> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val savedDtos = supabase.from("saved_projects")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<SavedProjectDto>()

            val projectIds = savedDtos.map { it.projectId }.distinct()
            if (projectIds.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val projectDtos = supabase.from("projects")
                .select {
                    filter {
                        isIn("id", projectIds)
                    }
                }
                .decodeList<ProjectDto>()

            Result.success(projectDtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val saveRequest = SaveProjectRequest(
                userId = userId,
                projectId = projectId
            )

            supabase.from("saved_projects")
                .insert(saveRequest)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unsaveProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            supabase.from("saved_projects")
                .delete {
                    filter {
                        eq("user_id", userId)
                        eq("project_id", projectId)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isProjectSaved(projectId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val saved = supabase.from("saved_projects")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("project_id", projectId)
                    }
                }
                .decodeList<SavedProjectDto>()

            Result.success(saved.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectById(id: String): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val dto = supabase.from("projects")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingle<ProjectDto>()
            val project = dto.toDomain()
            AppCache.putProject(project)
            Result.success(project)
        } catch (e: Exception) {
            val cached = AppCache.getProject(id)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun createProject(project: Project): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            val computedScore = if (project.revivalScore > 0) project.revivalScore else ScrapAIEngine.calculateRevivalScore(project)

            val projectToInsert = CreateProjectRequest(
                ownerId = userId,
                name = project.name,
                description = project.description,
                problem = project.problem.ifBlank { null },
                status = project.status.name,
                category = if (project.category.isNotBlank()) project.category else "Web",
                technologies = project.technologies,
                requiredSkills = project.requiredSkills,
                githubUrl = project.githubUrl.ifBlank { null },
                teamSize = if (project.teamSize > 0) project.teamSize else 4,
                revivalScore = computedScore
            )
            
            val createdDto = supabase.from("projects")
                .insert(projectToInsert) {
                    select()
                }
                .decodeSingle<ProjectDto>()

            val createdProject = createdDto.toDomain()
            AppCache.putProject(createdProject)

            // Automatically generate and persist initial ScrapAI Analysis for new projects
            try {
                val initialAnalysis = ScrapAIEngine.generateAnalysis(createdProject)
                supabase.from("project_ai_analyses").insert(initialAnalysis)
            } catch (ignored: Exception) {}
            
            Result.success(createdProject)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProject(project: Project): Result<Project> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))
            
            val updateRequest = UpdateProjectRequest(
                name = project.name,
                description = project.description,
                status = project.status.name,
                problem = project.problem.ifBlank { null },
                category = project.category.ifBlank { null },
                technologies = project.technologies,
                requiredSkills = project.requiredSkills,
                githubUrl = project.githubUrl.ifBlank { null }
            )

            val updatedDto = supabase.from("projects")
                .update(updateRequest) {
                    filter {
                        eq("id", project.id)
                        eq("owner_id", userId)
                    }
                    select()
                }
                .decodeSingle<ProjectDto>()
            
            val updatedProject = updatedDto.toDomain()
            AppCache.putProject(updatedProject)
            Result.success(updatedProject)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            // Clean up child tables to prevent foreign key constraint violations
            try { supabase.from("saved_projects").delete { filter { eq("project_id", id) } } } catch (_: Exception) {}
            try { supabase.from("project_ai_analyses").delete { filter { eq("project_id", id) } } } catch (_: Exception) {}
            try { supabase.from("tasks").delete { filter { eq("project_id", id) } } } catch (_: Exception) {}
            try { supabase.from("roadmap_items").delete { filter { eq("project_id", id) } } } catch (_: Exception) {}
            try { supabase.from("collaboration_requests").delete { filter { eq("project_id", id) } } } catch (_: Exception) {}
            try { supabase.from("project_members").delete { filter { eq("project_id", id) } } } catch (_: Exception) {}

            supabase.from("projects")
                .delete {
                    filter {
                        eq("id", id)
                        eq("owner_id", userId)
                    }
                }
            AppCache.removeProject(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateProjectAnalysis(projectId: String): Result<ProjectAnalysis> = withContext(Dispatchers.IO) {
        try {
            val existing = supabase.from("project_ai_analyses")
                .select {
                    filter { eq("project_id", projectId) }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<AnalysisDto>()

            if (existing != null) {
                return@withContext Result.success(existing.toDomain())
            }

            val projectResult = getProjectById(projectId)
            val project = projectResult.getOrNull()
            if (project != null) {
                val newAnalysisDto = ScrapAIEngine.generateAnalysis(project)
                try {
                    val saved = supabase.from("project_ai_analyses")
                        .insert(newAnalysisDto) { select() }
                        .decodeSingle<AnalysisDto>()
                    Result.success(saved.toDomain())
                } catch (e: Exception) {
                    Result.success(newAnalysisDto.toDomain())
                }
            } else {
                Result.failure(Exception("Project not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectAnalysis(projectId: String): Result<ProjectAnalysis?> = withContext(Dispatchers.IO) {
        try {
            val dto = supabase.from("project_ai_analyses")
                .select {
                    filter { eq("project_id", projectId) }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<AnalysisDto>()
            
            if (dto != null) {
                return@withContext Result.success(dto.toDomain())
            }

            val projectResult = getProjectById(projectId)
            val project = projectResult.getOrNull()
            if (project != null) {
                val newAnalysisDto = ScrapAIEngine.generateAnalysis(project)
                try {
                    supabase.from("project_ai_analyses").insert(newAnalysisDto)
                } catch (ignored: Exception) {}
                Result.success(newAnalysisDto.toDomain())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

