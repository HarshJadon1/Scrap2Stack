package com.scrap2stack.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.scrap2stack.app.core.network.RetrofitClient
import com.scrap2stack.app.core.network.SessionManager
import com.scrap2stack.app.data.repository.AuthRepository
import com.scrap2stack.app.feature.MainScreen
import com.scrap2stack.app.feature.auth.AuthViewModel
import com.scrap2stack.app.feature.auth.AuthViewModelFactory
import com.scrap2stack.app.feature.auth.ForgotPasswordScreen
import com.scrap2stack.app.feature.auth.LoginScreen
import com.scrap2stack.app.feature.auth.RegisterScreen
import com.scrap2stack.app.feature.charms.CharmsScreen
import com.scrap2stack.app.feature.matching.CollaborationRequestScreen
import com.scrap2stack.app.feature.matching.CollaborationRequestsScreen
import com.scrap2stack.app.feature.matching.DeveloperMatchingScreen
import com.scrap2stack.app.feature.onboarding.OnboardingScreen
import com.scrap2stack.app.feature.profile.DeveloperProfileScreen
import com.scrap2stack.app.feature.project.*
import com.scrap2stack.app.feature.settings.SettingsScreen
import com.scrap2stack.app.feature.splash.SplashScreen
import com.scrap2stack.app.feature.workspace.ProjectWorkspaceScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val apiService = remember { RetrofitClient.getApiService(context) }
    val authRepository = remember { AuthRepository(apiService, sessionManager) }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    val actions = remember(navController) { NavActions(navController) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                sessionManager = sessionManager,
                onNavigateToOnboarding = actions.navigateToOnboarding,
                onNavigateToLogin = actions.navigateToLogin,
                onNavigateToMain = actions.navigateToMain
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = authViewModel,
                onNavigateToLogin = actions.navigateToLogin,
                onNavigateToRegister = actions.navigateToRegister
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = actions.navigateToRegister,
                onNavigateToHome = actions.navigateToMain,
                onNavigateToForgotPassword = actions.navigateToForgotPassword
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = actions.navigateBack
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = actions.navigateBack,
                onNavigateToHome = actions.navigateToMain
            )
        }
        
        composable("main") {
            MainScreen(
                rootNavController = navController,
                onNavigateToProjectDetails = actions.navigateToProjectDetails,
                onNavigateToCharms = actions.navigateToCharms,
                onNavigateToSettings = actions.navigateToSettings,
                onNavigateToCreateProject = actions.navigateToCreateProject,
                onNavigateToCollaborationRequests = actions.navigateToCollaborationRequests
            )
        }
        
        composable(Screen.ProjectDetails.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailsScreen(
                projectId = projectId,
                onNavigateBack = actions.navigateBack,
                onNavigateToScrapAI = { actions.navigateToScrapAI(projectId) },
                onNavigateToMatches = { actions.navigateToDeveloperMatches(projectId) },
                onNavigateToWorkspace = { actions.navigateToWorkspace(projectId) }
            )
        }
        
        composable(Screen.CreateProject.route) {
            CreateProjectScreen(
                onNavigateBack = actions.navigateBack,
                onProjectCreated = actions.navigateToGitHubImport
            )
        }
        
        composable(Screen.GitHubImport.route) {
            GitHubImportScreen(
                onNavigateBack = actions.navigateBack,
                onNavigateToAnalysis = actions.navigateToScrapAI
            )
        }
        
        composable(Screen.ScrapAIAnalysis.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ScrapAIAnalysisScreen(
                projectId = projectId,
                onNavigateBack = actions.navigateBack,
                onNavigateToRevivalScore = { actions.navigateToRevivalScore(projectId) }
            )
        }
        
        composable(Screen.RevivalScore.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            RevivalScoreScreen(
                projectId = projectId,
                onNavigateBack = actions.navigateBack,
                onNavigateToRequiredSkills = { actions.navigateToRequiredSkills(projectId) }
            )
        }
        
        composable(Screen.RequiredSkills.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            RequiredSkillsScreen(
                projectId = projectId,
                onNavigateBack = actions.navigateBack,
                onNavigateToMatches = { actions.navigateToDeveloperMatches(projectId) }
            )
        }
        
        composable(Screen.DeveloperMatches.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            DeveloperMatchingScreen(
                projectId = projectId,
                onNavigateBack = actions.navigateBack,
                onNavigateToDeveloperProfile = actions.navigateToDeveloperProfile,
                onNavigateToRequestCollaboration = { developerId ->
                    actions.navigateToCollaborationRequest(projectId, developerId)
                }
            )
        }
        
        composable(Screen.DeveloperProfile.route) { backStackEntry ->
            val developerId = backStackEntry.arguments?.getString("developerId") ?: ""
            DeveloperProfileScreen(
                developerId = developerId,
                onNavigateBack = actions.navigateBack,
                onRequestCollaboration = actions.navigateBack
            )
        }
        
        composable(Screen.CollaborationRequest.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val developerId = backStackEntry.arguments?.getString("developerId") ?: ""
            CollaborationRequestScreen(
                projectId = projectId,
                developerId = developerId,
                onNavigateBack = actions.navigateBack,
                onRequestSent = actions.popBackStackToMain
            )
        }

        composable(Screen.CollaborationRequests.route) {
            CollaborationRequestsScreen(
                onNavigateBack = actions.navigateBack
            )
        }
        
        composable(Screen.Workspace.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectWorkspaceScreen(
                projectId = projectId,
                onNavigateBack = actions.navigateBack,
                onNavigateToMatches = { actions.navigateToDeveloperMatches(projectId) }
            )
        }
        
        composable(Screen.Charms.route) {
            CharmsScreen(onNavigateBack = actions.navigateBack)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = actions.navigateBack)
        }
    }
}

class NavActions(navController: NavHostController) {
    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }

    val navigateToOnboarding: () -> Unit = {
        navController.navigate(Screen.Onboarding.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    val navigateToRegister: () -> Unit = {
        navController.navigate(Screen.Register.route)
    }

    val navigateToForgotPassword: () -> Unit = {
        navController.navigate(Screen.ForgotPassword.route)
    }

    val navigateToMain: () -> Unit = {
        navController.navigate("main") {
            popUpTo(0) { inclusive = true }
        }
    }

    val navigateToProjectDetails: (String) -> Unit = { projectId ->
        navController.navigate(Screen.ProjectDetails.createRoute(projectId))
    }

    val navigateToCharms: () -> Unit = {
        navController.navigate(Screen.Charms.route)
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(Screen.Settings.route)
    }

    val navigateToCreateProject: () -> Unit = {
        navController.navigate(Screen.CreateProject.route)
    }

    val navigateToCollaborationRequests: () -> Unit = {
        navController.navigate(Screen.CollaborationRequests.route)
    }

    val navigateToScrapAI: (String) -> Unit = { projectId ->
        navController.navigate(Screen.ScrapAIAnalysis.createRoute(projectId))
    }

    val navigateToDeveloperMatches: (String) -> Unit = { projectId ->
        navController.navigate(Screen.DeveloperMatches.createRoute(projectId))
    }

    val navigateToWorkspace: (String) -> Unit = { projectId ->
        navController.navigate(Screen.Workspace.createRoute(projectId))
    }

    val navigateToGitHubImport: () -> Unit = {
        navController.navigate(Screen.GitHubImport.route)
    }

    val navigateToRevivalScore: (String) -> Unit = { projectId ->
        navController.navigate(Screen.RevivalScore.createRoute(projectId))
    }

    val navigateToRequiredSkills: (String) -> Unit = { projectId ->
        navController.navigate(Screen.RequiredSkills.createRoute(projectId))
    }

    val navigateToDeveloperProfile: (String) -> Unit = { developerId ->
        navController.navigate(Screen.DeveloperProfile.createRoute(developerId))
    }

    val navigateToCollaborationRequest: (String, String) -> Unit = { projectId, developerId ->
        navController.navigate(Screen.CollaborationRequest.createRoute(projectId, developerId))
    }

    val popBackStackToMain: () -> Unit = {
        navController.popBackStack("main", false)
    }
}
