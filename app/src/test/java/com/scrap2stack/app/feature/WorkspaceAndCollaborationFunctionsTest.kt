package com.scrap2stack.app.feature

import com.scrap2stack.app.core.network.GeminiAiService
import com.scrap2stack.app.core.network.LiveDataResult
import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.domain.model.ChatMessage
import com.scrap2stack.app.domain.model.CollaborationRequest
import com.scrap2stack.app.domain.model.CollaborationStatus
import com.scrap2stack.app.domain.model.ContributionType
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.model.NotificationItem
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.model.ProjectMember
import com.scrap2stack.app.domain.model.ProjectRole
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.domain.model.RoadmapItem
import com.scrap2stack.app.domain.model.Task
import com.scrap2stack.app.domain.model.TaskPriority
import com.scrap2stack.app.domain.model.TaskStatus
import com.scrap2stack.app.domain.repository.CharmsRepository
import com.scrap2stack.app.domain.repository.ChatRepository
import com.scrap2stack.app.domain.repository.CollaborationRepository
import com.scrap2stack.app.domain.repository.NotificationRepository
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.repository.TeamRepository
import com.scrap2stack.app.domain.repository.UserRepository
import com.scrap2stack.app.domain.repository.WorkspaceRepository
import com.scrap2stack.app.domain.service.MatchingEngine
import com.scrap2stack.app.domain.usecase.AcceptCollaborationRequestUseCase
import com.scrap2stack.app.domain.usecase.CancelCollaborationRequestUseCase
import com.scrap2stack.app.domain.usecase.GetDeveloperMatchesUseCase
import com.scrap2stack.app.domain.usecase.GetProjectMembersUseCase
import com.scrap2stack.app.domain.usecase.GetReceivedCollaborationRequestsUseCase
import com.scrap2stack.app.domain.usecase.GetSentCollaborationRequestsUseCase
import com.scrap2stack.app.domain.usecase.RejectCollaborationRequestUseCase
import com.scrap2stack.app.domain.usecase.SendCollaborationRequestUseCase
import com.scrap2stack.app.feature.chat.ChatUiState
import com.scrap2stack.app.feature.chat.ChatViewModel
import com.scrap2stack.app.feature.matching.CollaborationUiState
import com.scrap2stack.app.feature.matching.CollaborationViewModel
import com.scrap2stack.app.feature.matching.DeveloperMatchingViewModel
import com.scrap2stack.app.feature.matching.MatchingUiState
import com.scrap2stack.app.feature.matching.RequestsUiState
import com.scrap2stack.app.feature.notifications.NotificationsUiState
import com.scrap2stack.app.feature.notifications.NotificationsViewModel
import com.scrap2stack.app.feature.project.TeamUiState
import com.scrap2stack.app.feature.project.TeamViewModel
import com.scrap2stack.app.feature.workspace.WorkspaceState
import com.scrap2stack.app.feature.workspace.WorkspaceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceAndCollaborationFunctionsTest {

    class MainDispatcherRule(
        private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }
        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `WorkspaceViewModel manages tasks AI roadmap persistence charms and project shipping`() = runTest {
        val tasks = mutableListOf<Task>()
        val roadmap = mutableListOf<RoadmapItem>()
        val members = listOf(ProjectMember("m-1", "p-1", "u-me", ProjectRole.OWNER, "2026-11-20"))
        var project = Project(
            id = "p-1",
            ownerId = "u-me",
            name = "StackFlow",
            description = "Workflow app",
            problem = "Needs CI",
            category = "Web",
            technologies = listOf("Kotlin"),
            requiredSkills = listOf("Kotlin"),
            status = ProjectStatus.REVIVING
        )
        var taskCharmsCalls = 0
        var shippedCharmsCalls = 0

        val workspaceRepo = object : WorkspaceRepository {
            override suspend fun getTasks(projectId: String) = Result.success(tasks.toList())
            override suspend fun createTask(task: Task, projectId: String): Result<Task> {
                val created = task.copy(id = "t-${tasks.size + 1}")
                tasks.add(created)
                return Result.success(created)
            }
            override suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit> {
                val idx = tasks.indexOfFirst { it.id == taskId }
                if (idx >= 0) tasks[idx] = tasks[idx].copy(status = TaskStatus.fromString(status))
                return Result.success(Unit)
            }
            override suspend fun getRoadmapItems(projectId: String) = Result.success(roadmap.toList())
            override suspend fun saveRoadmapItems(projectId: String, items: List<RoadmapItem>): Result<List<RoadmapItem>> {
                roadmap.clear()
                val persisted = items.mapIndexed { i, item -> item.copy(id = "rm-${i + 1}") }
                roadmap.addAll(persisted)
                return Result.success(persisted)
            }
            override suspend fun updateRoadmapItemStatus(itemId: String, status: String): Result<Unit> {
                val idx = roadmap.indexOfFirst { it.id == itemId }
                if (idx >= 0) roadmap[idx] = roadmap[idx].copy(status = status)
                return Result.success(Unit)
            }
            override suspend fun getProjectMembers(projectId: String) = Result.success(members)
            override fun observeWorkspaceUpdates(projectId: String): Flow<Unit> = emptyFlow()
        }

        val projectRepo = object : ProjectRepository {
            override suspend fun getDiscoverProjects() = Result.success(listOf(project))
            override suspend fun getFeaturedProjects() = Result.success(listOf(project))
            override suspend fun getRecentProjects() = Result.success(listOf(project))
            override suspend fun getMyProjects() = Result.success(listOf(project))
            override suspend fun getJoinedProjects() = Result.success(emptyList<Project>())
            override suspend fun getCompletedProjects() = Result.success(emptyList<Project>())
            override suspend fun getSavedProjects() = Result.success(emptyList<Project>())
            override suspend fun saveProject(projectId: String) = Result.success(Unit)
            override suspend fun unsaveProject(projectId: String) = Result.success(Unit)
            override suspend fun isProjectSaved(projectId: String) = Result.success(false)
            override suspend fun getProjectById(id: String) = Result.success(project)
            override suspend fun createProject(project: Project) = Result.success(project)
            override suspend fun updateProject(updated: Project): Result<Project> {
                project = updated
                return Result.success(updated)
            }
            override suspend fun deleteProject(id: String) = Result.success(Unit)
            override suspend fun generateProjectAnalysis(projectId: String): Result<ProjectAnalysis> = Result.failure(Exception("Unused"))
            override suspend fun getProjectAnalysis(projectId: String): Result<ProjectAnalysis?> = Result.success(null)
        }

        val charmsRepo = object : CharmsRepository {
            override suspend fun getUserCharms() = Result.success(50)
            override suspend fun getContributionHistory() = Result.success(emptyList<CharmContribution>())
            override suspend fun awardTaskCompletionCharms(taskId: String, projectId: String): Result<Unit> {
                taskCharmsCalls++
                return Result.success(Unit)
            }
            override suspend fun awardProjectRevivedCharms(projectId: String) = Result.success(Unit)
            override suspend fun awardProjectShippedCharms(projectId: String): Result<Unit> {
                shippedCharmsCalls++
                return Result.success(Unit)
            }
        }

        val fakeGemini = object : GeminiAiService() {
            override suspend fun generateRevivalRoadmap(
                projectName: String,
                techStack: List<String>,
                problem: String
            ): Result<LiveDataResult<List<RoadmapItem>>> {
                return Result.success(
                    LiveDataResult(
                        data = listOf(
                            RoadmapItem(id = "", title = "Refactor API", description = "Clean DTOs", order = 1, status = "TODO", requiredSkills = listOf("Kotlin"))
                        ),
                        isLiveData = false,
                        statusNote = "Offline heuristic plan"
                    )
                )
            }
        }

        val vm = WorkspaceViewModel(workspaceRepo, projectRepo, charmsRepo, fakeGemini)
        vm.loadWorkspaceData("p-1")
        assertTrue(vm.uiState.value is WorkspaceState.Success)

        vm.createNewTask("Build Kanban", "Compose", TaskPriority.HIGH, "p-1")
        assertEquals(1, (vm.uiState.value as WorkspaceState.Success).tasks.size)

        vm.updateTaskStatus("t-1", "COMPLETED", "p-1")
        assertEquals(1, taskCharmsCalls)
        assertEquals(TaskStatus.COMPLETED, (vm.uiState.value as WorkspaceState.Success).tasks.first().status)

        vm.generateAiRoadmap("p-1")
        val afterRoadmap = vm.uiState.value as WorkspaceState.Success
        assertFalse(afterRoadmap.isRoadmapLiveData)
        assertEquals("Offline heuristic plan", afterRoadmap.roadmapStatusNote)
        assertEquals("rm-1", afterRoadmap.roadmapItems.first().id)

        vm.convertRoadmapPhaseToTasks(afterRoadmap.roadmapItems.first(), "p-1")
        assertEquals(3, (vm.uiState.value as WorkspaceState.Success).tasks.size)

        var shippedCallback = false
        vm.shipProject("p-1") { shippedCallback = true }
        val shippedState = vm.uiState.value as WorkspaceState.Success
        assertTrue(shippedCallback)
        assertTrue(shippedState.isShipped)
        assertEquals(ProjectStatus.COMPLETED, shippedState.project?.status)
        assertEquals(1, shippedCharmsCalls)
    }

    @Test
    fun `Chat Team Matching Collaboration and Notifications ViewModels handle full workflows`() = runTest {
        val me = Developer("u-1", "Harsh", "hj_001", skills = listOf("Kotlin", "Android"), experienceLevel = ExperienceLevel.ADVANCED)
        val peer = Developer("u-2", "Priya", "priya_k", skills = listOf("Kotlin", "UI/UX"), experienceLevel = ExperienceLevel.ADVANCED, charms = 500)
        val project = Project(
            id = "p-1",
            ownerId = "u-1",
            name = "Scrap2Stack",
            description = "Revival",
            problem = "Needs UI",
            category = "Mobile",
            technologies = listOf("Kotlin"),
            requiredSkills = listOf("Kotlin", "UI/UX")
        )

        // Chat
        val msgs = mutableListOf<ChatMessage>()
        val chatRepo = object : ChatRepository {
            override suspend fun getProjectMessages(projectId: String) = Result.success(msgs.toList())
            override suspend fun sendMessage(projectId: String, message: String): Result<ChatMessage> {
                val created = ChatMessage("m-${msgs.size + 1}", projectId, me.id, me.name, message, "2026-11-25T10:00:00Z")
                msgs.add(created)
                return Result.success(created)
            }
            override fun observeProjectMessages(projectId: String): Flow<Unit> = emptyFlow()
        }
        val userRepo = object : UserRepository {
            override suspend fun getMyProfile() = Result.success(me)
            override suspend fun updateProfile(developer: Developer) = Result.success(developer)
            override suspend fun getDevelopers() = Result.success(listOf(me, peer))
            override suspend fun getDeveloperById(id: String) = Result.success(peer)
        }
        val chatVm = ChatViewModel(chatRepo, userRepo)
        chatVm.sendMessage("p-1", "   ")
        chatVm.sendMessage("p-1", "Deploying PR!")
        val chatSuccess = chatVm.uiState.value as ChatUiState.Success
        assertEquals(1, chatSuccess.messages.size)
        assertEquals("u-1", chatSuccess.currentUserId)

        // Team
        val teamRepo = object : TeamRepository {
            override suspend fun getProjectMembers(projectId: String) = Result.success(
                listOf(ProjectMember("pm-1", projectId, me.id, ProjectRole.OWNER, "2026-11-20", user = me))
            )
        }
        val teamVm = TeamViewModel(GetProjectMembersUseCase(teamRepo))
        teamVm.loadTeam("p-1")
        assertEquals(1, (teamVm.uiState.value as TeamUiState.Success).members.size)

        // Matching
        val projRepo = object : ProjectRepository {
            override suspend fun getDiscoverProjects() = Result.success(listOf(project))
            override suspend fun getFeaturedProjects() = Result.success(listOf(project))
            override suspend fun getRecentProjects() = Result.success(listOf(project))
            override suspend fun getMyProjects() = Result.success(listOf(project))
            override suspend fun getJoinedProjects() = Result.success(emptyList<Project>())
            override suspend fun getCompletedProjects() = Result.success(emptyList<Project>())
            override suspend fun getSavedProjects() = Result.success(emptyList<Project>())
            override suspend fun saveProject(projectId: String) = Result.success(Unit)
            override suspend fun unsaveProject(projectId: String) = Result.success(Unit)
            override suspend fun isProjectSaved(projectId: String) = Result.success(false)
            override suspend fun getProjectById(id: String) = Result.success(project)
            override suspend fun createProject(project: Project) = Result.success(project)
            override suspend fun updateProject(project: Project) = Result.success(project)
            override suspend fun deleteProject(id: String) = Result.success(Unit)
            override suspend fun generateProjectAnalysis(projectId: String): Result<ProjectAnalysis> = Result.failure(Exception("Unused"))
            override suspend fun getProjectAnalysis(projectId: String): Result<ProjectAnalysis?> = Result.success(null)
        }
        val matchVm = DeveloperMatchingViewModel(GetDeveloperMatchesUseCase(projRepo, userRepo, MatchingEngine()))
        matchVm.loadMatches("p-1")
        assertTrue((matchVm.uiState.value as MatchingUiState.Success).matches.isNotEmpty())

        // Collaboration
        val received = mutableListOf(CollaborationRequest("cr-1", "p-1", peer.id, me.id, "Maintainer", "Join us", CollaborationStatus.PENDING, "2026-11-25"))
        val sent = mutableListOf<CollaborationRequest>()
        val collabRepo = object : CollaborationRepository {
            override suspend fun sendCollaborationRequest(projectId: String, receiverId: String, message: String): Result<CollaborationRequest> {
                val r = CollaborationRequest("cr-2", projectId, me.id, receiverId, "Contributor", message, CollaborationStatus.PENDING, "2026-11-25")
                sent.add(r)
                return Result.success(r)
            }
            override suspend fun getReceivedRequests() = Result.success(received.toList())
            override suspend fun getSentRequests() = Result.success(sent.toList())
            override suspend fun acceptRequest(requestId: String): Result<Unit> {
                received.replaceAll { if (it.id == requestId) it.copy(status = CollaborationStatus.ACCEPTED) else it }
                return Result.success(Unit)
            }
            override suspend fun rejectRequest(requestId: String): Result<Unit> {
                received.replaceAll { if (it.id == requestId) it.copy(status = CollaborationStatus.REJECTED) else it }
                return Result.success(Unit)
            }
            override suspend fun cancelRequest(requestId: String): Result<Unit> {
                sent.removeAll { it.id == requestId }
                return Result.success(Unit)
            }
            override fun observeCollaborationRequests(userId: String): Flow<Unit> = emptyFlow()
        }
        val collabVm = CollaborationViewModel(
            SendCollaborationRequestUseCase(collabRepo),
            GetReceivedCollaborationRequestsUseCase(collabRepo),
            GetSentCollaborationRequestsUseCase(collabRepo),
            AcceptCollaborationRequestUseCase(collabRepo),
            RejectCollaborationRequestUseCase(collabRepo),
            CancelCollaborationRequestUseCase(collabRepo),
            collabRepo
        )
        collabVm.sendRequest("p-1", peer.id, "Let's ship")
        assertTrue(collabVm.uiState.value is CollaborationUiState.Success)
        collabVm.acceptRequest("cr-1")
        assertEquals(CollaborationStatus.ACCEPTED, (collabVm.requestsState.value as RequestsUiState.Success).received.first().status)

        // Notifications
        val notifs = mutableListOf(
            NotificationItem("n-1", me.id, "Invite", "New invite", "COLLABORATION", read = false, createdAt = "2026-11-25T12:00:00Z"),
            NotificationItem("n-2", me.id, "Shipped", "Done", "MILESTONE", read = false, createdAt = "2026-11-24T12:00:00Z")
        )
        val notifRepo = object : NotificationRepository {
            override suspend fun getNotifications() = Result.success(notifs.toList())
            override suspend fun markAsRead(notificationId: String): Result<Unit> {
                notifs.replaceAll { if (it.id == notificationId) it.copy(read = true) else it }
                return Result.success(Unit)
            }
            override suspend fun markAllAsRead(): Result<Unit> {
                notifs.replaceAll { it.copy(read = true) }
                return Result.success(Unit)
            }
        }
        val emptyCharmsRepo = object : CharmsRepository {
            override suspend fun getUserCharms() = Result.success(0)
            override suspend fun getContributionHistory() = Result.success(emptyList<CharmContribution>())
            override suspend fun awardTaskCompletionCharms(taskId: String, projectId: String) = Result.success(Unit)
            override suspend fun awardProjectRevivedCharms(projectId: String) = Result.success(Unit)
            override suspend fun awardProjectShippedCharms(projectId: String) = Result.success(Unit)
        }
        val notifVm = NotificationsViewModel(notifRepo, emptyCharmsRepo)
        assertEquals(2, NotificationsViewModel.countUnread(notifVm.uiState.value))
        notifVm.markAsRead("n-1")
        assertEquals(1, NotificationsViewModel.countUnread(notifVm.uiState.value))
        notifVm.markAllAsRead()
        assertEquals(0, NotificationsViewModel.countUnread(notifVm.uiState.value))
        assertTrue((notifVm.uiState.value as NotificationsUiState.Success).notifications.all { it.read })
    }
}
