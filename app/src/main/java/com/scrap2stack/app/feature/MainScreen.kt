package com.scrap2stack.app.feature

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.scrap2stack.app.core.navigation.Screen
import com.scrap2stack.app.feature.discovery.DiscoveryScreen
import com.scrap2stack.app.feature.discovery.DiscoveryViewModel
import com.scrap2stack.app.feature.home.HomeScreen
import com.scrap2stack.app.feature.home.HomeViewModel
import com.scrap2stack.app.feature.matching.CollaborationViewModel
import com.scrap2stack.app.feature.notifications.NotificationsScreen
import com.scrap2stack.app.feature.notifications.NotificationsViewModel
import com.scrap2stack.app.feature.profile.ProfileScreen
import com.scrap2stack.app.feature.profile.ProfileViewModel
import com.scrap2stack.app.feature.project.MyProjectsScreen
import com.scrap2stack.app.feature.project.MyProjectsViewModel

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home")
    object Discover : BottomNavItem(Screen.Discover.route, Icons.Default.Search, "Discover")
    object MyProjects : BottomNavItem(Screen.MyProjects.route, Icons.Default.Work, "Projects")
    object Notifications : BottomNavItem(Screen.Notifications.route, Icons.Default.Notifications, "Alerts")
    object Profile : BottomNavItem(Screen.Profile.route, Icons.Default.AccountCircle, "Profile")
}

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    onNavigateToProjectDetails: (String) -> Unit,
    onNavigateToCharms: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToCollaborationRequests: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    profileViewModel: ProfileViewModel,
    collaborationViewModel: CollaborationViewModel,
    homeViewModel: HomeViewModel,
    discoveryViewModel: DiscoveryViewModel,
    myProjectsViewModel: MyProjectsViewModel,
    notificationsViewModel: NotificationsViewModel
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Discover,
        BottomNavItem.MyProjects,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == Screen.Home.route || currentRoute == Screen.MyProjects.route) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCreateProject,
                    icon = { Icon(Icons.Default.Work, contentDescription = null) },
                    text = { Text("New Project") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToProjectDetails = onNavigateToProjectDetails,
                    onNavigateToCharms = onNavigateToCharms
                )
            }
            composable(Screen.Discover.route) {
                DiscoveryScreen(
                    viewModel = discoveryViewModel,
                    onNavigateToProjectDetails = onNavigateToProjectDetails
                )
            }
            composable(Screen.MyProjects.route) {
                MyProjectsScreen(
                    viewModel = myProjectsViewModel,
                    onNavigateToProjectDetails = onNavigateToProjectDetails
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    notificationsViewModel = notificationsViewModel,
                    collaborationViewModel = collaborationViewModel,
                    onNavigateToRequests = onNavigateToCollaborationRequests
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToEditProfile = onNavigateToEditProfile
                )
            }
        }
    }
}
