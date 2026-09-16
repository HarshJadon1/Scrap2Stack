package com.scrap2stack.app.domain.repository

import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectAnalysis

interface ProjectRepository {
    suspend fun getDiscoverProjects(): Result<List<Project>>
    suspend fun getFeaturedProjects(): Result<List<Project>>
    suspend fun getRecentProjects(): Result<List<Project>>
    
    suspend fun getMyProjects(): Result<List<Project>>
    suspend fun getJoinedProjects(): Result<List<Project>>
    suspend fun getCompletedProjects(): Result<List<Project>>
    suspend fun getSavedProjects(): Result<List<Project>>
    suspend fun saveProject(projectId: String): Result<Unit>
    suspend fun unsaveProject(projectId: String): Result<Unit>
    suspend fun isProjectSaved(projectId: String): Result<Boolean>
    
    suspend fun getProjectById(id: String): Result<Project>
    suspend fun createProject(project: Project): Result<Project>
    suspend fun updateProject(project: Project): Result<Project>
    suspend fun deleteProject(id: String): Result<Unit>

    // ScrapAI
    suspend fun generateProjectAnalysis(projectId: String): Result<ProjectAnalysis>
    suspend fun getProjectAnalysis(projectId: String): Result<ProjectAnalysis?>
}
