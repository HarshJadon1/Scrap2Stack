package com.scrap2stack.app.data.repository

import com.scrap2stack.app.core.network.ApiService
import com.scrap2stack.app.data.remote.dto.*
import retrofit2.Response

class ProjectRepository(private val apiService: ApiService) {

    suspend fun getProjects(page: Int, search: String? = null, status: String? = null): Response<BaseResponse<ProjectPaginationResponse>> {
        return apiService.getProjects(page = page, search = search, status = status)
    }

    suspend fun getProjectDetails(id: String): Response<BaseResponse<ProjectDto>> {
        return apiService.getProjectDetails(id)
    }

    suspend fun createProject(request: CreateProjectRequest): Response<BaseResponse<ProjectDto>> {
        return apiService.createProject(request)
    }

    suspend fun updateProject(id: String, updateData: Map<String, Any>): Response<BaseResponse<ProjectDto>> {
        return apiService.updateProject(id, updateData)
    }

    suspend fun importRepository(url: String): Response<BaseResponse<ImportResponse>> {
        return apiService.importRepository(GitHubImportRequest(url))
    }

    suspend fun syncGitHub(projectId: String): Response<BaseResponse<Unit>> {
        return apiService.syncGitHub(projectId)
    }

    suspend fun getProjectAnalysis(projectId: String): Response<BaseResponse<AnalysisDto>> {
        return apiService.getProjectAnalysis(projectId)
    }

    suspend fun getRevivalScore(projectId: String): Response<BaseResponse<RevivalScoreDto>> {
        return apiService.getRevivalScore(projectId)
    }

    // Matching & Recommendations
    suspend fun getDeveloperMatches(
        projectId: String,
        page: Int = 1,
        limit: Int = 10,
        minScore: Int? = null,
        role: String? = null
    ): Response<BaseResponse<MatchingResponse>> {
        return apiService.getDeveloperMatches(projectId, page, limit, minScore, role)
    }

    suspend fun getRecommendedProjects(
        page: Int = 1,
        limit: Int = 10
    ): Response<BaseResponse<List<ProjectRecommendationDto>>> {
        return apiService.getRecommendedProjects(page, limit)
    }

    // Collaboration
    suspend fun sendCollaborationRequest(
        projectId: String,
        receiverId: String,
        proposedRole: String,
        message: String
    ): Response<BaseResponse<CollaborationRequestDto>> {
        return apiService.sendCollaborationRequest(
            projectId,
            SendCollabRequest(projectId, receiverId, proposedRole, message)
        )
    }

    suspend fun getReceivedRequests(): Response<BaseResponse<List<CollaborationRequestDto>>> {
        return apiService.getReceivedRequests()
    }

    suspend fun acceptRequest(requestId: String): Response<BaseResponse<Unit>> {
        return apiService.acceptRequest(requestId)
    }

    suspend fun rejectRequest(requestId: String): Response<BaseResponse<Unit>> {
        return apiService.rejectRequest(requestId)
    }

    // Teams
    suspend fun getTeamDetails(teamId: String): Response<BaseResponse<TeamDto>> {
        return apiService.getTeamDetails(teamId)
    }

    suspend fun getTeamMembers(teamId: String): Response<BaseResponse<List<TeamMemberDto>>> {
        return apiService.getTeamMembers(teamId)
    }

    suspend fun removeTeamMember(teamId: String, userId: String): Response<BaseResponse<Unit>> {
        return apiService.removeTeamMember(teamId, userId)
    }

    // Workspace
    suspend fun getWorkspace(projectId: String): Response<BaseResponse<WorkspaceDto>> {
        return apiService.getWorkspace(projectId)
    }

    suspend fun getProjectTasks(projectId: String): Response<BaseResponse<List<TaskDto>>> {
        return apiService.getProjectTasks(projectId)
    }

    suspend fun updateTaskStatus(taskId: String, status: String): Response<BaseResponse<Unit>> {
        return apiService.updateTaskStatus(taskId, UpdateTaskStatusRequest(status))
    }

    suspend fun getRoadmap(projectId: String): Response<BaseResponse<RoadmapResponse>> {
        return apiService.getRoadmap(projectId)
    }
    
    // User / Contributions
    suspend fun getMyContributions(): Response<BaseResponse<List<GitHubContributionDto>>> {
        return apiService.getMyContributions()
    }

    suspend fun getCharmBalance(): Response<BaseResponse<Map<String, Int>>> {
        return apiService.getCharmBalance()
    }
}
