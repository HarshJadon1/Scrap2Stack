package com.scrap2stack.app.feature

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.scrap2stack.app.core.navigation.Screen
import com.scrap2stack.app.core.ui.components.InAppAlert
import com.scrap2stack.app.core.ui.components.InAppNotificationBanner
import com.scrap2stack.app.core.ui.components.bouncingClickable
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
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoDark

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home")
    object Discover : BottomNavItem(Screen.Discover.route, Icons.Default.Explore, "Discover")
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

    var activeAlert by remember { mutableStateOf<InAppAlert?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Floating Glass Dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.22f),
                                        Color.White.copy(alpha = 0.05f),
                                        ElectricMint.copy(alpha = 0.15f)
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF0C1322).copy(alpha = 0.92f),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                            val activeColor by animateColorAsState(
                                targetValue = if (selected) ElectricMint else Color(0xFF94A3B8),
                                label = "nav_color"
                            )
                            val pillBgColor by animateColorAsState(
                                targetValue = if (selected) ElectricMint.copy(alpha = 0.12f) else Color.Transparent,
                                label = "pill_bg"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(pillBgColor)
                                    .bouncingClickable(scaleDown = 0.92f) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(24.dp),
                                            tint = activeColor
                                        )
                                        // Badge dot on Notifications tab
                                        if (item == BottomNavItem.Notifications) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .align(Alignment.TopEnd)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF43F5E))
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = activeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == Screen.Home.route || currentRoute == Screen.MyProjects.route) {
                Box(
                    modifier = Modifier
                        .bouncingClickable(scaleDown = 0.94f, onClick = onNavigateToCreateProject)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PrimaryIndigo, PrimaryIndigoDark)
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Project",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                    }
                }
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

    InAppNotificationBanner(
        alert = activeAlert,
        onDismiss = { activeAlert = null },
        onClick = {
            navController.navigate(Screen.Notifications.route)
        }
    )
}
}
