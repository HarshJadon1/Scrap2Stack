package com.scrap2stack.app.feature

import com.scrap2stack.app.domain.model.CollaborationRequest
import com.scrap2stack.app.domain.model.CollaborationStatus
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.model.Importance
import com.scrap2stack.app.domain.model.MissingComponent
import com.scrap2stack.app.domain.model.Priority
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.model.ProjectRisk
import com.scrap2stack.app.domain.model.RequiredSkillRecommendation
import com.scrap2stack.app.domain.model.RoadmapStep
import com.scrap2stack.app.domain.repository.CollaborationRepository
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.repository.UserRepository
import com.scrap2stack.app.domain.usecase.AcceptCollaborationRequestUseCase
import com.scrap2stack.app.domain.usecase.GetReceivedCollaborationRequestsUseCase
import com.scrap2stack.app.domain.usecase.RejectCollaborationRequestUseCase
import com.scrap2stack.app.feature.matching.CollaborationRequestsState
import com.scrap2stack.app.feature.matching.CollaborationRequestsViewModel
import com.scrap2stack.app.feature.profile.DeveloperProfileUiState
import com.scrap2stack.app.feature.profile.DeveloperProfileViewModel
import com.scrap2stack.app.feature.project.RequiredSkillsState
import com.scrap2stack.app.feature.project.RequiredSkillsViewModel
import com.scrap2stack.app.feature.project.RevivalScoreState
import com.scrap2stack.app.feature.project.RevivalScoreViewModel
import com.scrap2stack.app.feature.project.ScrapAIAnalysisState
import com.scrap2stack.app.feature.project.ScrapAIAnalysisViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class SecondaryViewModelsFunctionsTest {

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
    fun collaborationRequestsViewModelHandlesEmptySuccessAcceptRejectAndError() = runTest {
        val requests = mutableListOf(
            CollaborationRequest("cr-1", "p-1", "u-2", "u-1", "Maintainer", "Let me help", CollaborationStatus.PENDING, "2026-11-26"),
            CollaborationRequest("cr-2", "p-1", "u-3", "u-1", "Contributor", "Happy to QA", CollaborationStatus.PENDING, "2026-11-26")
        )
        var failLoad = false

        val repo = object : CollaborationRepository {
            override suspend fun sendCollaborationRequest(projectId: String, receiverId: String, message: String) =
                Result.failure<CollaborationRequest>(Exception("Unused"))
            override suspend fun getReceivedRequests(): Result<List<CollaborationRequest>> =
                if (failLoad) Result.failure(Exception("Network down")) else Result.success(requests.toList())
            override suspend fun getSentRequests() = Result.success(emptyList<CollaborationRequest>())
            override suspend fun acceptRequest(requestId: String): Result<Unit> {
                requests.replaceAll { if (it.id == requestId) it.copy(status = CollaborationStatus.ACCEPTED) else it }
                return Result.success(Unit)
            }
            override suspend fun rejectRequest(requestId: String): Result<Unit> {
                requests.replaceAll { if (it.id == requestId) it.copy(status = CollaborationStatus.REJECTED) else it }
                return Result.success(Unit)
            }
            override suspend fun cancelRequest(requestId: String) = Result.success(Unit)
            override fun observeCollaborationRequests(userId: String): Flow<Unit> = emptyFlow()
        }

        val vm = CollaborationRequestsViewModel(
            GetReceivedCollaborationRequestsUseCase(repo),
            AcceptCollaborationRequestUseCase(repo),
            RejectCollaborationRequestUseCase(repo)
        )

        vm.loadRequests()
        assertEquals(2, (vm.uiState.value as CollaborationRequestsState.Success).requests.size)

        vm.acceptRequest("cr-1")
        assertEquals(
            CollaborationStatus.ACCEPTED,
            (vm.uiState.value as CollaborationRequestsState.Success).requests.first { it.id == "cr-1" }.status
        )

        vm.rejectRequest("cr-2")
        assertEquals(
            CollaborationStatus.REJECTED,
            (vm.uiState.value as CollaborationRequestsState.Success).requests.first { it.id == "cr-2" }.status
        )

        requests.clear()
        vm.loadRequests()
        assertTrue(vm.uiState.value is CollaborationRequestsState.Empty)

        failLoad = true
        vm.loadRequests()
        assertEquals("Network down", (vm.uiState.value as CollaborationRequestsState.Error).message)
    }

    @Test
    fun developerProfileAndProjectAiAnalysisViewModelsHandleSuccessNullAndFailure() = runTest {
        val peer = Developer("u-9", "Linus", "torvalds", skills = listOf("C", "Linux"), experienceLevel = ExperienceLevel.ADVANCED)
        val project = Project("p-9", "u-9", "Kernel Revive", "Bootloader cleanup", revivalScore = 92)
        val analysis = ProjectAnalysis(
            id = "a-9",
            projectId = "p-9",
            projectSummary = "Strong core bootloader",
            currentState = "Builds locally",
            missingComponents = listOf(MissingComponent("Tests", "Add QEMU boot test", Priority.HIGH)),
            requiredSkills = listOf(RequiredSkillRecommendation("C", Importance.REQUIRED)),
            roadmap = listOf(RoadmapStep(1, "QEMU Harness", "Automate smoke boot", Priority.HIGH)),
            revivalScore = 92,
            scoreExplanation = "Clear scope",
            risks = listOf(ProjectRisk("Hardware matrix", Priority.MEDIUM, "Only x86 verified")),
            recommendations = listOf("Add CI matrix"),
            analyzedAt = "2026-11-26"
        )

        val userRepo = object : UserRepository {
            override suspend fun getMyProfile() = Result.success(peer)
            override suspend fun updateProfile(developer: Developer) = Result.success(developer)
            override suspend fun getDevelopers() = Result.success(listOf(peer))
            override suspend fun getDeveloperById(id: String): Result<Developer> =
                if (id == "u-9") Result.success(peer) else Result.failure(Exception("Developer missing"))
        }
        val devProfileVm = DeveloperProfileViewModel(userRepo)
        devProfileVm.loadDeveloperProfile("u-9")
        assertEquals("Linus", (devProfileVm.uiState.value as DeveloperProfileUiState.Success).developer.name)
        devProfileVm.loadDeveloperProfile("missing")
        assertEquals("Developer missing", (devProfileVm.uiState.value as DeveloperProfileUiState.Error).message)

        var returnNullAnalysis = false
        var failRepo = false
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
            override suspend fun getProjectById(id: String): Result<Project> =
                if (failRepo) Result.failure(Exception("Project fetch failed")) else Result.success(project)
            override suspend fun createProject(project: Project) = Result.success(project)
            override suspend fun updateProject(project: Project) = Result.success(project)
            override suspend fun deleteProject(id: String) = Result.success(Unit)
            override suspend fun generateProjectAnalysis(projectId: String) = Result.success(analysis)
            override suspend fun getProjectAnalysis(projectId: String): Result<ProjectAnalysis?> = when {
                failRepo -> Result.failure(Exception("Analysis fetch failed"))
                returnNullAnalysis -> Result.success(null)
                else -> Result.success(analysis)
            }
        }

        val analysisVm = ScrapAIAnalysisViewModel(projectRepo)
        val skillsVm = RequiredSkillsViewModel(projectRepo)
        val scoreVm = RevivalScoreViewModel(projectRepo)

        analysisVm.loadAnalysis("p-9")
        assertEquals(92, (analysisVm.uiState.value as ScrapAIAnalysisState.Success).analysis.revivalScore)

        skillsVm.loadSkills("p-9")
        assertEquals("C", (skillsVm.uiState.value as RequiredSkillsState.Success).analysis.requiredSkills.first().skill)

        scoreVm.loadRevivalScore("p-9")
        assertEquals("Kernel Revive", (scoreVm.uiState.value as RevivalScoreState.Success).project.name)

        returnNullAnalysis = true
        analysisVm.loadAnalysis("p-9")
        skillsVm.loadSkills("p-9")
        assertTrue(analysisVm.uiState.value is ScrapAIAnalysisState.Error)
        assertTrue(skillsVm.uiState.value is RequiredSkillsState.Error)

        failRepo = true
        analysisVm.loadAnalysis("p-9")
        skillsVm.loadSkills("p-9")
        scoreVm.loadRevivalScore("p-9")
        assertEquals("Analysis fetch failed", (analysisVm.uiState.value as ScrapAIAnalysisState.Error).message)
        assertEquals("Analysis fetch failed", (skillsVm.uiState.value as RequiredSkillsState.Error).message)
        assertEquals("Project fetch failed", (scoreVm.uiState.value as RevivalScoreState.Error).message)
    }
}
