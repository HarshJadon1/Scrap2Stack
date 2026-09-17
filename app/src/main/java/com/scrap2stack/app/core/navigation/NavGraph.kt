package com.scrap2stack.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.scrap2stack.app.core.network.SessionManager
import com.scrap2stack.app.data.repository.AuthRepositoryImpl
import com.scrap2stack.app.data.repository.CharmsRepositoryImpl
import com.scrap2stack.app.data.repository.CollaborationRepositoryImpl
import com.scrap2stack.app.data.repository.NotificationRepositoryImpl
import com.scrap2stack.app.data.repository.ProjectRepositoryImpl
import com.scrap2stack.app.data.repository.UserRepositoryImpl
import com.scrap2stack.app.data.repository.WorkspaceRepositoryImpl
import com.scrap2stack.app.domain.repository.AuthRepository
import com.scrap2stack.app.domain.repository.CharmsRepository
import com.scrap2stack.app.domain.repository.NotificationRepository
import com.scrap2stack.app.domain.repository.ProjectRepository
import com.scrap2stack.app.domain.repository.UserRepository
import com.scrap2stack.app.domain.repository.WorkspaceRepository
import com.scrap2stack.app.domain.service.MatchingEngine
import com.scrap2stack.app.domain.usecase.*
import com.scrap2stack.app.feature.MainScreen
import com.scrap2stack.app.feature.auth.AuthViewModel
import com.scrap2stack.app.feature.auth.AuthViewModelFactory
import com.scrap2stack.app.feature.auth.ForgotPasswordScreen
import com.scrap2stack.app.feature.auth.LoginScreen
import com.scrap2stack.app.feature.auth.RegisterScreen
import com.scrap2stack.app.feature.charms.CharmsScreen
import com.scrap2stack.app.feature.charms.CharmsViewModel
import com.scrap2stack.app.feature.charms.CharmsViewModelFactory
import com.scrap2stack.app.feature.discovery.DiscoveryViewModel
import com.scrap2stack.app.feature.discovery.DiscoveryViewModelFactory
import com.scrap2stack.app.feature.home.HomeViewModel
import com.scrap2stack.app.feature.home.HomeViewModelFactory
import com.scrap2stack.app.feature.matching.*
import com.scrap2stack.app.feature.notifications.NotificationsViewModel
import com.scrap2stack.app.feature.notifications.NotificationsViewModelFactory
import com.scrap2stack.app.feature.onboarding.OnboardingScreen
import com.scrap2stack.app.feature.profile.DeveloperProfileScreen
import com.scrap2stack.app.feature.profile.DeveloperProfileViewModel
import com.scrap2stack.app.feature.profile.DeveloperProfileViewModelFactory
import com.scrap2stack.app.feature.profile.EditProfileScreen
import com.scrap2stack.app.feature.profile.ProfileViewModel
import com.scrap2stack.app.feature.profile.ProfileViewModelFactory
import com.scrap2stack.app.feature.project.*
import com.scrap2stack.app.feature.settings.SettingsScreen
import com.scrap2stack.app.feature.splash.SplashScreen
import com.scrap2stack.app.feature.workspace.ProjectWorkspaceScreen
import com.scrap2stack.app.feature.workspace.WorkspaceViewModel
import com.scrap2stack.app.feature.workspace.WorkspaceViewModelFactory

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context) }
    val authRepository: AuthRepository = remember { AuthRepositoryImpl(sessionManager) }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    // Repositories
    val userRepository: UserRepository = remember { UserRepositoryImpl() }
    val projectRepository: ProjectRepository = remember { ProjectRepositoryImpl() }
    val collaborationRepository = remember { CollaborationRepositoryImpl() }
    val charmsRepository: CharmsRepository = remember { CharmsRepositoryImpl() }
    val notificationRepository: NotificationRepository = remember { NotificationRepositoryImpl() }
    val workspaceRepository: WorkspaceRepository = remember { WorkspaceRepositoryImpl() }

    val matchingEngine = remember { MatchingEngine() }
    val getDeveloperMatchesUseCase = remember {
        GetDeveloperMatchesUseCase(projectRepository, userRepository, matchingEngine)
    }

    val actions = remember(navController) { NavActions(navController) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = actions.navigateToOnboarding,
                onNavigateToLogin = actions.navigateToLogin,
                onNavigateToMain = actions.navigateToMain,
                authViewModel = authViewModel
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
                viewModel = authViewModel,
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
            val getMyProfileUseCase = remember { GetMyProfileUseCase(userRepository) }
            val updateProfileUseCase = remember { UpdateProfileUseCase(userRepository) }
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(getMyProfileUseCase, updateProfileUseCase)
            )

            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(projectRepository, userRepository)
            )
            
            val discoveryViewModel: DiscoveryViewModel = viewModel(
                factory = DiscoveryViewModelFactory(projectRepository)
            )
            
            val myProjectsViewModel: MyProjectsViewModel = viewModel(
                factory = MyProjectsViewModelFactory(projectRepository)
            )

            val collaborationViewModel: CollaborationViewModel = viewModel(
                factory = CollaborationViewModelFactory(
                    sendCollaborationRequestUseCase = SendCollaborationRequestUseCase(collaborationRepository),
                    getReceivedRequestsUseCase = GetReceivedCollaborationRequestsUseCase(collaborationRepository),
                    getSentRequestsUseCase = GetSentCollaborationRequestsUseCase(collaborationRepository),
                    acceptRequestUseCase = AcceptCollaborationRequestUseCase(collaborationRepository),
                    rejectRequestUseCase = RejectCollaborationRequestUseCase(collaborationRepository),
                    cancelRequestUseCase = CancelCollaborationRequestUseCase(collaborationRepository),
                    collaborationRepository = collaborationRepository
                )
            )

            val notificationsViewModel: NotificationsViewModel = viewModel(
                factory = NotificationsViewModelFactory(notificationRepository)
            )

            MainScreen(
                rootNavController = navController,
                onNavigateToProjectDetails = actions.navigateToProjectDetails,
                onNavigateToCharms = actions.navigateToCharms,
                onNavigateToSettings = actions.navigateToSettings,
                onNavigateToCreateProject = actions.navigateToCreateProject,
                onNavigateToCollaborationRequests = actions.navigateToCollaborationRequests,
                onNavigateToEditProfile = actions.navigateToEditProfile,
                profileViewModel = profileViewModel,
                collaborationViewModel = collaborationViewModel,
                homeViewModel = homeViewModel,
                discoveryViewModel = discoveryViewModel,
                myProjectsViewModel = myProjectsViewModel,
                notificationsViewModel = notificationsViewModel
            )
        }
        
        composable(Screen.EditProfile.route) {
            val getMyProfileUseCase = remember { GetMyProfileUseCase(userRepository) }
            val updateProfileUseCase = remember { UpdateProfileUseCase(userRepository) }
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModelFactory(getMyProfileUseCase, updateProfileUseCase)
            )
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = actions.navigateBack
            )
        }

        composable(Screen.ProjectDetails.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val getProjectDetailsUseCase = remember { GetProjectDetailsUseCase(projectRepository) }
            val deleteProjectUseCase = remember { DeleteProjectUseCase(projectRepository) }
            val projectDetailsViewModel: ProjectDetailsViewModel = viewModel(
                factory = ProjectDetailsViewModelFactory(
                    getProjectDetailsUseCase,
                    deleteProjectUseCase,
                    projectRepository,
                    userRepository
                )
            )
            ProjectDetailsScreen(
                projectId = projectId,
                viewModel = projectDetailsViewModel,
                onNavigateBack = actions.navigateBack,
                onNavigateToScrapAI = { actions.navigateToScrapAI(projectId) },
                onNavigateToMatches = { actions.navigateToDeveloperMatches(projectId) },
                onNavigateToWorkspace = { actions.navigateToWorkspace(projectId) }
            )
        }
        
        composable(Screen.CreateProject.route) {
            val createProjectUseCase = remember { CreateProjectUseCase(projectRepository) }
            val createProjectViewModel: CreateProjectViewModel = viewModel(
                factory = CreateProjectViewModelFactory(createProjectUseCase)
            )
            CreateProjectScreen(
                viewModel = createProjectViewModel,
                onNavigateBack = actions.navigateBack,
                onProjectCreated = { projectId ->
                    actions.navigateToProjectDetails(projectId)
                }
            )
        }
        
        composable(Screen.GitHubImport.route) {
            val gitHubImportViewModel: GitHubImportViewModel = viewModel(
                factory = GitHubImportViewModelFactory(projectRepository)
            )
            GitHubImportScreen(
                viewModel = gitHubImportViewModel,
                onNavigateBack = actions.navigateBack,
                onNavigateToAnalysis = actions.navigateToScrapAI
            )
        }
        
        composable(Screen.ScrapAIAnalysis.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val scrapAIAnalysisViewModel: ScrapAIAnalysisViewModel = viewModel(
                factory = ScrapAIAnalysisViewModelFactory(projectRepository)
            )
            ScrapAIAnalysisScreen(
                projectId = projectId,
                viewModel = scrapAIAnalysisViewModel,
                onNavigateBack = actions.navigateBack,
                onNavigateToRevivalScore = { actions.navigateToRevivalScore(projectId) }
            )
        }
        
        composable(Screen.RevivalScore.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val revivalScoreViewModel: RevivalScoreViewModel = viewModel(
                factory = RevivalScoreViewModelFactory(projectRepository)
            )
            RevivalScoreScreen(
                projectId = projectId,
                viewModel = revivalScoreViewModel,
                onNavigateBack = actions.navigateBack,
                onNavigateToRequiredSkills = { actions.navigateToRequiredSkills(projectId) }
            )
        }
        
        composable(Screen.RequiredSkills.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val requiredSkillsViewModel: RequiredSkillsViewModel = viewModel(
                factory = RequiredSkillsViewModelFactory(projectRepository)
            )
            RequiredSkillsScreen(
                projectId = projectId,
                viewModel = requiredSkillsViewModel,
                onNavigateBack = actions.navigateBack,
                onNavigateToMatches = { actions.navigateToDeveloperMatches(projectId) }
            )
        }
        
        composable(Screen.DeveloperMatches.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val matchingViewModel: DeveloperMatchingViewModel = viewModel(
                factory = DeveloperMatchingViewModelFactory(getDeveloperMatchesUseCase)
            )
            DeveloperMatchingScreen(
                projectId = projectId,
                viewModel = matchingViewModel,
                onNavigateBack = actions.navigateBack,
                onNavigateToDeveloperProfile = { developerId ->
                    actions.navigateToDeveloperProfile(developerId, projectId)
                },
                onNavigateToRequestCollaboration = { developerId ->
                    actions.navigateToCollaborationRequest(projectId, developerId)
                }
            )
        }
        
        composable(
            route = Screen.DeveloperProfile.route,
            arguments = listOf(
                navArgument("developerId") { type = NavType.StringType },
                navArgument("projectId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val developerId = backStackEntry.arguments?.getString("developerId") ?: ""
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val devProfileViewModel: DeveloperProfileViewModel = viewModel(
                factory = DeveloperProfileViewModelFactory(userRepository)
            )
            DeveloperProfileScreen(
                developerId = developerId,
                viewModel = devProfileViewModel,
                onNavigateBack = actions.navigateBack,
                onRequestCollaboration = {
                    if (projectId.isNotBlank()) {
                        actions.navigateToCollaborationRequest(projectId, developerId)
                    } else {
                        actions.navigateBack()
                    }
                }
            )
        }
        
        composable(Screen.CollaborationRequest.route) { backStackEntry ->
            val collaborationViewModel: CollaborationViewModel = viewModel(
                factory = CollaborationViewModelFactory(
                    sendCollaborationRequestUseCase = SendCollaborationRequestUseCase(collaborationRepository),
                    getReceivedRequestsUseCase = GetReceivedCollaborationRequestsUseCase(collaborationRepository),
                    getSentRequestsUseCase = GetSentCollaborationRequestsUseCase(collaborationRepository),
                    acceptRequestUseCase = AcceptCollaborationRequestUseCase(collaborationRepository),
                    rejectRequestUseCase = RejectCollaborationRequestUseCase(collaborationRepository),
                    cancelRequestUseCase = CancelCollaborationRequestUseCase(collaborationRepository),
                    collaborationRepository = collaborationRepository
                )
            )
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val developerId = backStackEntry.arguments?.getString("developerId") ?: ""
            CollaborationRequestScreen(
                projectId = projectId,
                developerId = developerId,
                viewModel = collaborationViewModel,
                onNavigateBack = actions.navigateBack,
                onRequestSent = actions.popBackStackToMain
            )
        }

        composable(Screen.CollaborationRequests.route) {
            val collaborationViewModel: CollaborationViewModel = viewModel(
                factory = CollaborationViewModelFactory(
                    sendCollaborationRequestUseCase = SendCollaborationRequestUseCase(collaborationRepository),
                    getReceivedRequestsUseCase = GetReceivedCollaborationRequestsUseCase(collaborationRepository),
                    getSentRequestsUseCase = GetSentCollaborationRequestsUseCase(collaborationRepository),
                    acceptRequestUseCase = AcceptCollaborationRequestUseCase(collaborationRepository),
                    rejectRequestUseCase = RejectCollaborationRequestUseCase(collaborationRepository),
                    cancelRequestUseCase = CancelCollaborationRequestUseCase(collaborationRepository),
                    collaborationRepository = collaborationRepository
                )
            )
            CollaborationRequestsScreen(
                viewModel = collaborationViewModel,
                onNavigateBack = actions.navigateBack
            )
        }
        
        composable(Screen.Workspace.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val workspaceViewModel: WorkspaceViewModel = viewModel(
                factory = WorkspaceViewModelFactory(workspaceRepository)
            )
            ProjectWorkspaceScreen(
                projectId = projectId,
                viewModel = workspaceViewModel,
                onNavigateBack = actions.navigateBack,
                onNavigateToMatches = { actions.navigateToDeveloperMatches(projectId) }
            )
        }
        
        composable(Screen.Charms.route) {
            val charmsViewModel: CharmsViewModel = viewModel(
                factory = CharmsViewModelFactory(charmsRepository)
            )
            CharmsScreen(
                viewModel = charmsViewModel,
                onNavigateBack = actions.navigateBack
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = actions.navigateBack,
                onNavigateToProfile = actions.navigateToEditProfile,
                onNavigateToGitHub = actions.navigateToGitHubImport,
                onLogout = {
                    authViewModel.logout()
                    actions.navigateToLogin()
                }
            )
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
            popUpTo(0) { inclusive = true }
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

    val navigateToEditProfile: () -> Unit = {
        navController.navigate(Screen.EditProfile.route)
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

    val navigateToDeveloperProfile: (String, String) -> Unit = { developerId, projectId ->
        navController.navigate(Screen.DeveloperProfile.createRoute(developerId, projectId))
    }

    val navigateToCollaborationRequest: (String, String) -> Unit = { projectId, developerId ->
        navController.navigate(Screen.CollaborationRequest.createRoute(projectId, developerId))
    }

    val popBackStackToMain: () -> Unit = {
        navController.popBackStack("main", false)
    }
}
