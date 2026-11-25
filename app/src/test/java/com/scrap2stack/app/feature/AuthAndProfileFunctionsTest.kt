package com.scrap2stack.app.feature

import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.domain.model.ContributionType
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.domain.repository.AuthRepository
import com.scrap2stack.app.domain.repository.CharmsRepository
import com.scrap2stack.app.domain.repository.UserRepository
import com.scrap2stack.app.domain.usecase.GetMyProfileUseCase
import com.scrap2stack.app.domain.usecase.UpdateProfileUseCase
import com.scrap2stack.app.feature.auth.AuthState
import com.scrap2stack.app.feature.auth.AuthViewModel
import com.scrap2stack.app.feature.charms.CharmsUiState
import com.scrap2stack.app.feature.charms.CharmsViewModel
import com.scrap2stack.app.feature.profile.EditProfileUiState
import com.scrap2stack.app.feature.profile.ProfileUiState
import com.scrap2stack.app.feature.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
class AuthAndProfileFunctionsTest {

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

    private class FakeAuthRepository : AuthRepository {
        var shouldFail = false
        var loggedIn = false
        val onboardingFlow = MutableStateFlow(false)

        override suspend fun login(email: String, password: String): Result<Unit> {
            return if (shouldFail) Result.failure(Exception("Invalid credentials"))
            else {
                loggedIn = true
                Result.success(Unit)
            }
        }

        override suspend fun register(name: String, username: String, email: String, password: String): Result<Unit> {
            return if (shouldFail) Result.failure(Exception("Email already registered"))
            else {
                loggedIn = true
                onboardingFlow.value = true
                Result.success(Unit)
            }
        }

        override suspend fun logout() {
            loggedIn = false
        }

        override suspend fun forgotPassword(email: String): Result<Unit> {
            return if (shouldFail) Result.failure(Exception("Unknown account"))
            else Result.success(Unit)
        }

        override suspend fun isUserLoggedIn(): Boolean = loggedIn
        override fun isOnboardingCompleted(): Flow<Boolean> = onboardingFlow
        override suspend fun setOnboardingCompleted(completed: Boolean) {
            onboardingFlow.value = completed
        }
        override fun getCurrentUserId(): String? = if (loggedIn) "u-1" else null
        override fun getAccessToken(): String? = if (loggedIn) "tok-1" else null
    }

    private class FakeUserRepository(var profile: Developer) : UserRepository {
        var failLoad = false
        override suspend fun getMyProfile(): Result<Developer> =
            if (failLoad) Result.failure(Exception("Offline")) else Result.success(profile)

        override suspend fun updateProfile(developer: Developer): Result<Developer> {
            profile = developer
            return Result.success(developer)
        }

        override suspend fun getDevelopers(): Result<List<Developer>> = Result.success(listOf(profile))
        override suspend fun getDeveloperById(id: String): Result<Developer> = Result.success(profile)
    }

    private class FakeCharmsRepository : CharmsRepository {
        var history = listOf(
            CharmContribution("ch-1", "u-1", "p-1", ContributionType.TASK_COMPLETED, 10, "Shipped task", null, "2026-11-25T10:00:00Z", "Scrap2Stack"),
            CharmContribution("ch-2", "u-1", "p-1", ContributionType.PROJECT_SHIPPED, 100, "Shipped project", null, "2026-11-24T10:00:00Z", "Scrap2Stack")
        )
        override suspend fun getUserCharms(): Result<Int> = Result.success(history.sumOf { it.charms })
        override suspend fun getContributionHistory(): Result<List<CharmContribution>> = Result.success(history)
        override suspend fun awardTaskCompletionCharms(taskId: String, projectId: String): Result<Unit> = Result.success(Unit)
        override suspend fun awardProjectRevivedCharms(projectId: String): Result<Unit> = Result.success(Unit)
        override suspend fun awardProjectShippedCharms(projectId: String): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun `AuthViewModel validates empty inputs and tracks login register reset logout flows`() = runTest {
        val repo = FakeAuthRepository()
        val vm = AuthViewModel(repo)

        vm.login("", "secret")
        assertTrue(vm.authState.value is AuthState.Error)

        vm.login("dev@scrap2stack.app", "secret")
        assertTrue(vm.authState.value is AuthState.Success)
        assertTrue(vm.isUserLoggedIn())

        vm.resetState()
        assertTrue(vm.authState.value is AuthState.Idle)

        vm.forgotPassword("")
        assertTrue(vm.authState.value is AuthState.Error)

        vm.forgotPassword("dev@scrap2stack.app")
        assertTrue(vm.authState.value is AuthState.Success)

        vm.logout()
        assertFalse(vm.isUserLoggedIn())
        assertTrue(vm.authState.value is AuthState.Idle)

        vm.setOnboardingCompleted(true)
        assertTrue(vm.isOnboardingCompleted().first())
    }

    @Test
    fun `ProfileViewModel loads profile plus contributions and updates profile preserving history`() = runTest {
        val initialDev = Developer(
            id = "u-1",
            name = "Harsh Jadon",
            username = "hj_001",
            skills = listOf("Kotlin"),
            experienceLevel = ExperienceLevel.ADVANCED,
            charms = 110
        )
        val userRepo = FakeUserRepository(initialDev)
        val charmsRepo = FakeCharmsRepository()
        val vm = ProfileViewModel(
            GetMyProfileUseCase(userRepo),
            UpdateProfileUseCase(userRepo),
            charmsRepo
        )

        val loaded = vm.uiState.value as ProfileUiState.Success
        assertEquals("hj_001", loaded.developer.username)
        assertEquals(2, loaded.contributions.size)

        vm.updateProfile(
            name = "Harsh J",
            username = "hj_001",
            bio = "Android & Kotlin Engineer",
            skills = listOf("Kotlin", "Jetpack Compose"),
            interests = listOf("Open Source"),
            experienceLevel = ExperienceLevel.ADVANCED,
            githubUrl = "https://github.com/HarshJadon1",
            linkedinUrl = "",
            portfolioUrl = ""
        )

        assertTrue(vm.editUiState.value is EditProfileUiState.Success)
        val updated = vm.uiState.value as ProfileUiState.Success
        assertEquals("Harsh J", updated.developer.name)
        assertEquals(listOf("Kotlin", "Jetpack Compose"), updated.developer.skills)
        assertEquals(2, updated.contributions.size)
    }

    @Test
    fun `CharmsViewModel aggregates total charms and contribution ledger`() = runTest {
        val charmsVm = CharmsViewModel(FakeCharmsRepository())
        val state = charmsVm.uiState.value as CharmsUiState.Success
        assertEquals(110, state.totalCharms)
        assertEquals(2, state.history.size)
    }
}
