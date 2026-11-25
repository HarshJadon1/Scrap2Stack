package com.scrap2stack.app.feature

import com.scrap2stack.app.core.network.GitHubService
import com.scrap2stack.app.core.network.LiveDataResult
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.model.GitHubRepoInfo
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.repository.UserRepository
import com.scrap2stack.app.domain.service.ScrapAIEngine
import com.scrap2stack.app.domain.usecase.CreateProjectUseCase
import com.scrap2stack.app.domain.usecase.DeleteProjectUseCase
import com.scrap2stack.app.domain.usecase.GetProjectDetailsUseCase
import com.scrap2stack.app.domain.usecase.UpdateProjectUseCase
import com.scrap2stack.app.feature.discovery.DiscoveryUiState
import com.scrap2stack.app.feature.discovery.DiscoveryViewModel
import com.scrap2stack.app.feature.home.HomeUiState
import com.scrap2stack.app.feature.home.HomeViewModel
import com.scrap2stack.app.feature.project.CreateProjectUiState
import com.scrap2stack.app.feature.project.CreateProjectViewModel
import com.scrap2stack.app.feature.project.GitHubImportState
import com.scrap2stack.app.feature.project.GitHubImportViewModel
import com.scrap2stack.app.feature.project.MyProjectsUiState
import com.scrap2stack.app.feature.project.MyProjectsViewModel
import com.scrap2stack.app.feature.project.ProjectAnalysisState
import com.scrap2stack.app.feature.project.ProjectDetailsState
import com.scrap2stack.app.feature.project.ProjectDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ProjectAndDiscoveryFunctionsTest {

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

    private class FakeProjectRepository : ProjectRepository {
        val projects = mutableListOf(
            Project(
                id = "p-1",
                ownerId = "u-me",
                name = "Revive Note",
                description = "Offline markdown notes app",
                problem = "Stale Gradle setup",
                category = "Mobile",
                technologies = listOf("Kotlin", "Compose"),
                requiredSkills = listOf("Kotlin"),
                status = ProjectStatus.ABANDONED,
                revivalScore = 88
            ),
            Project(
                id = "p-2",
                ownerId = "u-other",
                name = "CLI Tasker",
                description = "Terminal task planner",
                problem = "Needs Rust refactor",
                category = "DevOps",
                technologies = listOf("Rust"),
                requiredSkills = listOf("Rust"),
                status = ProjectStatus.COMPLETED,
                revivalScore = 95
            )
        )
        val savedIds = mutableSetOf("p-2")
        var failCreate = false

        override suspend fun getDiscoverProjects(): Result<List<Project>> = Result.success(projects.toList())
        override suspend fun getFeaturedProjects(): Result<List<Project>> = Result.success(projects.sortedByDescending { it.revivalScore })
        override suspend fun getRecentProjects(): Result<List<Project>> = Result.success(projects.toList())
        override suspend fun getMyProjects(): Result<List<Project>> = Result.success(projects.filter { it.ownerId == "u-me" })
        override suspend fun getJoinedProjects(): Result<List<Project>> = Result.success(projects.filter { it.id == "p-2" })
        override suspend fun getCompletedProjects(): Result<List<Project>> = Result.success(projects.filter { it.status == ProjectStatus.COMPLETED })
        override suspend fun getSavedProjects(): Result<List<Project>> = Result.success(projects.filter { it.id in savedIds })
        override suspend fun saveProject(projectId: String): Result<Unit> {
            savedIds.add(projectId)
            return Result.success(Unit)
        }
        override suspend fun unsaveProject(projectId: String): Result<Unit> {
            savedIds.remove(projectId)
            return Result.success(Unit)
        }
        override suspend fun isProjectSaved(projectId: String): Result<Boolean> = Result.success(projectId in savedIds)
        override suspend fun getProjectById(id: String): Result<Project> {
            val found = projects.firstOrNull { it.id == id }
            return if (found != null) Result.success(found) else Result.failure(Exception("Not found"))
        }
        override suspend fun createProject(project: Project): Result<Project> {
            if (failCreate) return Result.failure(Exception("DB permission denied"))
            val created = project.copy(id = "p-${projects.size + 1}", ownerId = "u-me")
            projects.add(created)
            return Result.success(created)
        }
        override suspend fun updateProject(project: Project): Result<Project> {
            val idx = projects.indexOfFirst { it.id == project.id }
            if (idx >= 0) projects[idx] = project
            return Result.success(project)
        }
        override suspend fun deleteProject(id: String): Result<Unit> {
            projects.removeAll { it.id == id }
            return Result.success(Unit)
        }
        override suspend fun generateProjectAnalysis(projectId: String): Result<ProjectAnalysis> = getProjectAnalysis(projectId).let { res ->
            val a = res.getOrNull() ?: return Result.failure(Exception("Missing"))
            Result.success(a)
        }
        override suspend fun getProjectAnalysis(projectId: String): Result<ProjectAnalysis?> {
            val proj = projects.firstOrNull { it.id == projectId } ?: return Result.success(null)
            return Result.success(
                ProjectAnalysis(
                    id = "an-$projectId",
                    projectId = projectId,
                    revivalScore = ScrapAIEngine.calculateRevivalScore(proj),
                    scoreExplanation = "Viable stack",
                    projectSummary = proj.description,
                    currentState = "In progress",
                    missingComponents = emptyList(),
                    requiredSkills = emptyList(),
                    roadmap = emptyList(),
                    risks = emptyList(),
                    recommendations = listOf("Add unit tests"),
                    analyzedAt = "2026-11-26"
                )
            )
        }
    }

    private class FakeUserRepo : UserRepository {
        private val me = Developer("u-me", "Harsh", "hj_001", experienceLevel = ExperienceLevel.ADVANCED)
        override suspend fun getMyProfile(): Result<Developer> = Result.success(me)
        override suspend fun updateProfile(developer: Developer): Result<Developer> = Result.success(developer)
        override suspend fun getDevelopers(): Result<List<Developer>> = Result.success(listOf(me))
        override suspend fun getDeveloperById(id: String): Result<Developer> = Result.success(me)
    }

    @Test
    fun `Home Discovery and MyProjects ViewModels load categorized project feeds`() = runTest {
        val projectRepo = FakeProjectRepository()
        val userRepo = FakeUserRepo()

        val homeVm = HomeViewModel(projectRepo, userRepo)
        val homeState = homeVm.uiState.value as HomeUiState.Success
        assertEquals("hj_001", homeState.user?.username)
        assertEquals(2, homeState.recommendedProjects.size)

        val discoveryVm = DiscoveryViewModel(projectRepo)
        val discState = discoveryVm.uiState.value as DiscoveryUiState.Success
        assertEquals(2, discState.projects.size)

        val myVm = MyProjectsViewModel(projectRepo)
        val myState = myVm.uiState.value as MyProjectsUiState.Success
        assertEquals(1, myState.data.created.size)
        assertEquals(1, myState.data.joined.size)
        assertEquals(1, myState.data.saved.size)
        assertEquals(1, myState.data.completed.size)
    }

    @Test
    fun `CreateProjectViewModel validates required fields and persists valid projects`() = runTest {
        val repo = FakeProjectRepository()
        val vm = CreateProjectViewModel(CreateProjectUseCase(repo))

        vm.createProject("", "problem", "desc", "Web", 2, listOf("Kotlin"), ProjectStatus.ABANDONED, listOf("Kotlin"))
        assertTrue(vm.uiState.value is CreateProjectUiState.Error)

        vm.createProject("StackBot", "No docs", "AI PR helper", "AI/ML", 3, listOf("Python"), ProjectStatus.REVIVING, listOf("FastAPI"))
        val success = vm.uiState.value as CreateProjectUiState.Success
        assertEquals("StackBot", success.project.name)
        assertEquals(3, repo.projects.size)
    }

    @Test
    fun `GitHubImportViewModel validates URL fetches repo metadata and handles save failure cleanly`() = runTest {
        val repo = FakeProjectRepository()
        val fakeGitHub = object : GitHubService() {
            override suspend fun fetchRepoInfo(owner: String, repo: String): Result<LiveDataResult<GitHubRepoInfo>> {
                return Result.success(
                    LiveDataResult(
                        data = GitHubRepoInfo(
                            owner = owner,
                            name = repo,
                            description = "Compose canvas playground",
                            url = "https://github.com/$owner/$repo",
                            defaultBranch = "main",
                            primaryLanguage = "Kotlin",
                            stars = 120,
                            forks = 14,
                            openIssues = 2,
                            updatedAt = "2026-11-25"
                        ),
                        isLiveData = true
                    )
                )
            }
        }
        val vm = GitHubImportViewModel(repo, fakeGitHub)

        vm.importRepository("https://example.com/not-github")
        assertTrue(vm.uiState.value is GitHubImportState.Error)

        vm.importRepository("https://github.com/HarshJadon1/stack-canvas")
        val success = vm.uiState.value as GitHubImportState.Success
        assertEquals("Stack Canvas", success.project.name)
        assertEquals("Mobile", success.project.category)
        assertEquals("Compose canvas playground", success.project.description)

        repo.failCreate = true
        vm.importRepository("https://github.com/HarshJadon1/other-repo")
        assertTrue(vm.uiState.value is GitHubImportState.Error)
    }

    @Test
    fun `ProjectDetailsViewModel handles ownership bookmark toggle update and delete`() = runTest {
        val repo = FakeProjectRepository()
        val vm = ProjectDetailsViewModel(
            GetProjectDetailsUseCase(repo),
            DeleteProjectUseCase(repo),
            UpdateProjectUseCase(repo),
            repo,
            FakeUserRepo()
        )

        vm.loadProjectDetails("p-1")
        vm.loadProjectAnalysis("p-1")

        val loaded = vm.uiState.value as ProjectDetailsState.Success
        assertTrue(loaded.isOwner)
        assertFalse(loaded.isSaved)
        assertTrue(vm.analysisState.value is ProjectAnalysisState.Success)

        vm.toggleSave("p-1")
        assertTrue((vm.uiState.value as ProjectDetailsState.Success).isSaved)

        vm.updateProject(loaded.project.copy(name = "Revive Note Pro"))
        assertEquals("Revive Note Pro", (vm.uiState.value as ProjectDetailsState.Success).project.name)

        vm.deleteProject("p-1")
        assertTrue(vm.uiState.value is ProjectDetailsState.Deleted)
        assertTrue(repo.projects.none { it.id == "p-1" })
    }
}
