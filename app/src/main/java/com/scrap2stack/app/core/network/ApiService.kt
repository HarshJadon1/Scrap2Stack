package com.scrap2stack.app.core.network

import com.scrap2stack.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Scrap2Stack REST API Service
 */
interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResponse<AuthResponse>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<BaseResponse<AuthResponse>>

    // Projects
    @GET("projects")
    suspend fun getProjects(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<BaseResponse<ProjectPaginationResponse>>

    @GET("projects/{id}")
    suspend fun getProjectDetails(@Path("id") id: String): Response<BaseResponse<ProjectDto>>

    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<BaseResponse<ProjectDto>>

    @PUT("projects/{id}")
    suspend fun updateProject(@Path("id") id: String, @Body updateData: Map<String, Any>): Response<BaseResponse<ProjectDto>>

    // GitHub Integration
    @POST("github/import")
    suspend fun importRepository(@Body request: GitHubImportRequest): Response<BaseResponse<ImportResponse>>

    @POST("projects/{id}/github/sync")
    suspend fun syncGitHub(@Path("id") projectId: String): Response<BaseResponse<Unit>>

    // Analysis & Scoring
    @GET("projects/{id}/analysis")
    suspend fun getProjectAnalysis(@Path("id") id: String): Response<BaseResponse<AnalysisDto>>

    @GET("projects/{id}/revival-score")
    suspend fun getRevivalScore(@Path("id") id: String): Response<BaseResponse<RevivalScoreDto>>

    // Matching & Recommendations
    @GET("projects/{id}/matches")
    suspend fun getDeveloperMatches(
        @Path("id") projectId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("minScore") minScore: Int? = null,
        @Query("role") role: String? = null
    ): Response<BaseResponse<MatchingResponse>>

    @GET("users/me/recommended-projects")
    suspend fun getRecommendedProjects(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<BaseResponse<List<ProjectRecommendationDto>>>

    // Collaboration Requests
    @POST("projects/{id}/collaboration-requests")
    suspend fun sendCollaborationRequest(
        @Path("id") projectId: String,
        @Body request: SendCollabRequest
    ): Response<BaseResponse<CollaborationRequestDto>>

    @GET("collaboration-requests")
    suspend fun getReceivedRequests(): Response<BaseResponse<List<CollaborationRequestDto>>>

    @PUT("collaboration-requests/{id}/accept")
    suspend fun acceptRequest(@Path("id") requestId: String): Response<BaseResponse<Unit>>

    @PUT("collaboration-requests/{id}/reject")
    suspend fun rejectRequest(@Path("id") requestId: String): Response<BaseResponse<Unit>>

    // Teams
    @GET("teams/{id}")
    suspend fun getTeamDetails(@Path("id") teamId: String): Response<BaseResponse<TeamDto>>

    @GET("teams/{id}/members")
    suspend fun getTeamMembers(@Path("id") teamId: String): Response<BaseResponse<List<TeamMemberDto>>>

    @DELETE("teams/{id}/members/{userId}")
    suspend fun removeTeamMember(@Path("id") teamId: String, @Path("userId") userId: String): Response<BaseResponse<Unit>>

    // Workspace
    @GET("projects/{id}/workspace")
    suspend fun getWorkspace(@Path("id") projectId: String): Response<BaseResponse<WorkspaceDto>>

    @POST("projects/{id}/workspace")
    suspend fun createWorkspace(@Path("id") projectId: String, @Body request: Map<String, String>): Response<BaseResponse<WorkspaceDto>>

    @PUT("workspaces/{id}/sync")
    suspend fun syncWorkspaceProgress(@Path("id") workspaceId: String): Response<BaseResponse<Map<String, Double>>>

    // Tasks
    @GET("projects/{id}/tasks")
    suspend fun getProjectTasks(@Path("id") projectId: String): Response<BaseResponse<List<TaskDto>>>

    @POST("projects/{id}/tasks")
    suspend fun createTask(@Path("id") projectId: String, @Body request: CreateTaskRequest): Response<BaseResponse<TaskDto>>

    @PUT("tasks/{id}/status")
    suspend fun updateTaskStatus(@Path("id") taskId: String, @Body request: UpdateTaskStatusRequest): Response<BaseResponse<Unit>>

    // Roadmap
    @GET("projects/{id}/roadmap")
    suspend fun getRoadmap(@Path("id") projectId: String): Response<BaseResponse<RoadmapResponse>>

    @POST("projects/{id}/roadmap/generate")
    suspend fun generateAIRoadmap(@Path("id") projectId: String): Response<BaseResponse<RoadmapResponse>>

    // Users
    @GET("users/me")
    suspend fun getMyProfile(): Response<BaseResponse<UserDto>>

    @PUT("users/me")
    suspend fun updateProfile(@Body updateData: Map<String, Any>): Response<BaseResponse<UserDto>>

    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): Response<BaseResponse<UserDto>>

    @GET("users/me/contributions")
    suspend fun getMyContributions(): Response<BaseResponse<List<GitHubContributionDto>>>

    @GET("users/me/charms")
    suspend fun getCharmBalance(): Response<BaseResponse<Map<String, Int>>>
}
