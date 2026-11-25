package com.scrap2stack.app.feature

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
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
import com.scrap2stack.app.ui.theme.BrutalBorderWhite
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.BrutalSurfaceDark
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo

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

            FloatingBottomNavBar(
                items = items,
                currentRouteChecker = { route ->
                    currentDestination?.hierarchy?.any { it.route == route } == true
                },
                onSelectItem = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == Screen.Home.route || currentRoute == Screen.MyProjects.route) {
                Box(
                    modifier = Modifier
                        .tactileBrutalCard(
                            cornerRadius = 14.dp,
                            containerColor = PrimaryIndigo,
                            borderColor = BrutalBorderWhite,
                            borderWidth = 2.dp,
                            shadowColor = ElectricMint,
                            shadowOffsetX = 4.dp,
                            shadowOffsetY = 4.dp,
                            onClick = onNavigateToCreateProject
                        )
                        .padding(horizontal = 18.dp, vertical = 13.dp)
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
                            text = "NEW PROJECT",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
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
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
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

/**
 * Neo-Brutalist Stack Pop keycap bottom navigation dock with equal-width slots and inset safety.
 */
@Composable
private fun FloatingBottomNavBar(
    items: List<BottomNavItem>,
    currentRouteChecker: (String) -> Boolean,
    onSelectItem: (BottomNavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Hardware deck base with 2dp crisp white border and 4dp black offset shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .brutalCard(
                        cornerRadius = 18.dp,
                        containerColor = BrutalSurfaceDark,
                        borderColor = BrutalBorderWhite,
                        borderWidth = 2.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 4.dp,
                        shadowOffsetY = 4.dp
                    )
            )

            // Unclipped keycap row so active tab physically pops upward above the deck frame
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 6.dp, end = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    FloatingBottomNavItem(
                        item = item,
                        selected = currentRouteChecker(item.route),
                        onClick = { onSelectItem(item) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

/**
 * Single equal-width mechanical keycap destination inside [FloatingBottomNavBar].
 */
@Composable
private fun FloatingBottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liftOffset by animateDpAsState(
        targetValue = if (selected) (-6).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keycap_lift"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) BrutalInkBlack else Color(0xFFCBD5E1),
        label = "nav_color"
    )
    val keycapBg by animateColorAsState(
        targetValue = if (selected) ElectricMint else Color.Transparent,
        label = "keycap_bg"
    )
    val keycapBorder by animateColorAsState(
        targetValue = if (selected) BrutalShadowBlack else Color.Transparent,
        label = "keycap_border"
    )
    val keycapShadow by animateColorAsState(
        targetValue = if (selected) BrutalShadowBlack else Color.Transparent,
        label = "keycap_shadow"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 2.dp, vertical = 6.dp)
            .graphicsLayer {
                translationY = liftOffset.toPx()
            }
            .brutalCard(
                cornerRadius = 12.dp,
                containerColor = keycapBg,
                borderColor = keycapBorder,
                borderWidth = 2.dp,
                shadowColor = keycapShadow,
                shadowOffsetX = 2.dp,
                shadowOffsetY = 2.dp
            )
            .padding(horizontal = 2.dp, vertical = 4.dp),
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
                    modifier = Modifier.size(21.dp),
                    tint = contentColor
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 0.sp,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold
                ),
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
