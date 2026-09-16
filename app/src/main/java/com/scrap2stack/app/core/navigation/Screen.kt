package com.scrap2stack.app.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    // Main Bottom Nav
    object Home : Screen("home")
    object Discover : Screen("discover")
    object MyProjects : Screen("my_projects")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    
    // Profile Related
    object EditProfile : Screen("edit_profile")
    
    // Project Related
    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }
    object CreateProject : Screen("create_project")
    object GitHubImport : Screen("github_import")
    object ScrapAIAnalysis : Screen("scrap_ai_analysis/{projectId}") {
        fun createRoute(projectId: String) = "scrap_ai_analysis/$projectId"
    }
    object RevivalScore : Screen("revival_score/{projectId}") {
        fun createRoute(projectId: String) = "revival_score/$projectId"
    }
    object RequiredSkills : Screen("required_skills/{projectId}") {
        fun createRoute(projectId: String) = "required_skills/$projectId"
    }
    
    // Developer Related
    object DeveloperMatches : Screen("developer_matches/{projectId}") {
        fun createRoute(projectId: String) = "developer_matches/$projectId"
    }
    object DeveloperProfile : Screen("developer_profile/{developerId}") {
        fun createRoute(developerId: String) = "developer_profile/$developerId"
    }
    object CollaborationRequest : Screen("collaboration_request/{projectId}/{developerId}") {
        fun createRoute(projectId: String, developerId: String) = "collaboration_request/$projectId/$developerId"
    }
    object CollaborationRequests : Screen("collaboration_requests")
    
    // Team and Workspace
    object Team : Screen("team/{projectId}") {
        fun createRoute(projectId: String) = "team/$projectId"
    }
    object Workspace : Screen("workspace/{projectId}") {
        fun createRoute(projectId: String) = "workspace/$projectId"
    }
    
    // Feature screens
    object Charms : Screen("charms")
    object Settings : Screen("settings")
}
