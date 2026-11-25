package com.scrap2stack.app.feature.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.feature.matching.CollaborationUiState
import com.scrap2stack.app.feature.matching.CollaborationViewModel
import com.scrap2stack.app.feature.matching.RequestsUiState
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalBorderWhite
import com.scrap2stack.app.ui.theme.BrutalCanvasDark
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.BrutalSurfaceDark
import com.scrap2stack.app.ui.theme.BrutalSurfaceRaised
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    notificationsViewModel: NotificationsViewModel,
    collaborationViewModel: CollaborationViewModel,
    onNavigateToRequests: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val notificationsUiState by notificationsViewModel.uiState.collectAsState()
    val requestsState by collaborationViewModel.requestsState.collectAsState()
    val collabUiState by collaborationViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Received", "Sent", "Activity")

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0 || selectedTab == 1) {
            collaborationViewModel.loadRequests()
        } else {
            notificationsViewModel.loadNotifications()
        }
    }

    LaunchedEffect(collabUiState) {
        when (val state = collabUiState) {
            is CollaborationUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                collaborationViewModel.resetState()
            }
            is CollaborationUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                collaborationViewModel.resetState()
            }
            else -> {}
        }
    }

    // Fallback Mock Data for immediate offline & demonstration interactivity
    val fallbackReceivedRequests = remember {
        mutableStateListOf(
            CollaborationRequest(
                id = "req-rec-1",
                projectId = "p-1",
                senderId = "dev-101",
                receiverId = "user-me",
                proposedRole = "Android Architect",
                message = "Hey! I saw your Jetpack Compose skills on Scrap2Stack. Would love to have you help revive our open-source codebase!",
                status = CollaborationStatus.PENDING,
                createdAt = "10 mins ago",
                project = Project(
                    id = "p-1",
                    ownerId = "dev-101",
                    name = "Scrap2Stack Mobile",
                    description = "Collaborative app revival ecosystem",
                    problem = "Modern UI revamp needed",
                    category = "Mobile",
                    status = ProjectStatus.REVIVING,
                    technologies = listOf("Kotlin", "Compose"),
                    requiredSkills = listOf("Android", "UI/UX"),
                    revivalScore = 88,
                    lastActivity = "Today",
                    teamSize = 3,
                    githubUrl = "https://github.com/Scrap2Stack/app"
                ),
                sender = Developer(
                    id = "dev-101",
                    name = "Arjun Patel",
                    username = "arjun_codes",
                    bio = "Full Stack Engineer & OSS Contributor",
                    skills = listOf("Kotlin", "Compose", "Supabase"),
                    experienceLevel = ExperienceLevel.ADVANCED,
                    charms = 1450
                )
            ),
            CollaborationRequest(
                id = "req-rec-2",
                projectId = "p-2",
                senderId = "dev-102",
                receiverId = "user-me",
                proposedRole = "Backend Maintainer",
                message = "We need help setting up Supabase RLS policies and authentication session handling.",
                status = CollaborationStatus.PENDING,
                createdAt = "2 hours ago",
                project = Project(
                    id = "p-2",
                    ownerId = "dev-102",
                    name = "DevMatch Hub",
                    description = "Developer pairing portal",
                    problem = "Missing secure backend rules",
                    category = "Web",
                    status = ProjectStatus.REVIVING,
                    technologies = listOf("PostgreSQL", "Supabase"),
                    requiredSkills = listOf("Backend"),
                    revivalScore = 76,
                    lastActivity = "Yesterday",
                    teamSize = 2,
                    githubUrl = "https://github.com/Scrap2Stack/hub"
                ),
                sender = Developer(
                    id = "dev-102",
                    name = "Priya Sharma",
                    username = "priya_dev",
                    bio = "Backend Enthusiast",
                    skills = listOf("PostgreSQL", "Docker", "Go"),
                    experienceLevel = ExperienceLevel.INTERMEDIATE,
                    charms = 920
                )
            )
        )
    }

    val fallbackSentRequests = remember {
        mutableStateListOf(
            CollaborationRequest(
                id = "req-sent-1",
                projectId = "p-3",
                senderId = "user-me",
                receiverId = "dev-103",
                proposedRole = "Lead Designer",
                message = "Invited to join Scrap2Stack revival team for Design System overhaul.",
                status = CollaborationStatus.PENDING,
                createdAt = "1 day ago",
                project = Project(
                    id = "p-3",
                    ownerId = "user-me",
                    name = "CyberStack UI",
                    description = "Glassmorphic component library",
                    problem = "Component documentation missing",
                    category = "UI/UX",
                    status = ProjectStatus.REVIVING,
                    technologies = listOf("Figma", "Jetpack Compose"),
                    requiredSkills = listOf("UI/UX"),
                    revivalScore = 91,
                    lastActivity = "Today",
                    teamSize = 2,
                    githubUrl = ""
                ),
                receiver = Developer(
                    id = "dev-103",
                    name = "Rohan Verma",
                    username = "rohan_design",
                    bio = "UI/UX Product Designer",
                    skills = listOf("Figma", "Design Systems"),
                    experienceLevel = ExperienceLevel.ADVANCED,
                    charms = 1100
                )
            )
        )
    }

    val fallbackActivities = remember {
        listOf(
            NotificationItem(
                id = "act-1",
                title = "+10 Charms Earned",
                content = "Completed Kanban Task: 'Implement CodeBlockBubble with syntax highlighting'",
                type = "CHARMS",
                read = false,
                createdAt = "Just now"
            ),
            NotificationItem(
                id = "act-2",
                title = "Milestone Achieved: Phase 1 Complete",
                content = "Codebase Architecture & Dependency Modernization cleared for Scrap2Stack target.",
                type = "MILESTONE",
                read = false,
                createdAt = "1 hour ago"
            ),
            NotificationItem(
                id = "act-3",
                title = "New GitHub Activity Synced",
                content = "Merged PR #14: Realtime team chat and Gemini code explainer integrated.",
                type = "GITHUB",
                read = true,
                createdAt = "Yesterday"
            ),
            NotificationItem(
                id = "act-4",
                title = "+25 Charms Earned",
                content = "First project AI Revival Roadmap generated successfully with Gemini.",
                type = "CHARMS",
                read = true,
                createdAt = "2 days ago"
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BrutalCanvasDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .dotGridBackground()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVITY FEED //",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.sp
                        ),
                        color = ElectricMint
                    )
                    Text(
                        text = "Alerts & Collabs",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Requests, invites, and realtime team activity",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                }

                if (selectedTab == 2) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = BrutalSurfaceRaised,
                                borderColor = BrutalBorderWhite,
                                borderWidth = 2.dp,
                                shadowColor = ElectricMint,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = { notificationsViewModel.markAllAsRead() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all as read",
                            tint = ElectricMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Neo-Brutalist 3-Segment Tab Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .brutalCard(
                        cornerRadius = 14.dp,
                        containerColor = BrutalSurfaceDark,
                        borderColor = BrutalBorderWhite,
                        borderWidth = 2.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 4.dp,
                        shadowOffsetY = 4.dp
                    )
                    .padding(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        val pendingCount = if (index == 0) {
                            val remoteCount = (requestsState as? RequestsUiState.Success)?.received?.count { it.status == CollaborationStatus.PENDING } ?: 0
                            if (remoteCount > 0) remoteCount else fallbackReceivedRequests.count { it.status == CollaborationStatus.PENDING }
                        } else 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (isSelected) {
                                        Modifier.brutalCard(
                                            cornerRadius = 10.dp,
                                            containerColor = ElectricMint,
                                            borderColor = BrutalShadowBlack,
                                            borderWidth = 1.5.dp,
                                            shadowColor = BrutalShadowBlack,
                                            shadowOffsetX = 2.dp,
                                            shadowOffsetY = 2.dp
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) BrutalInkBlack else Color(0xFFCBD5E1),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                )
                                if (pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) BrutalInkBlack else ElectricMint),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$pendingCount",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) ElectricMint else BrutalInkBlack,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // RECEIVED REQUESTS
                    val currentReceived = when (val state = requestsState) {
                        is RequestsUiState.Success -> if (state.received.isNotEmpty()) state.received else fallbackReceivedRequests
                        else -> fallbackReceivedRequests
                    }

                    if (currentReceived.isEmpty()) {
                        EmptyAlertState(
                            icon = Icons.Default.Inbox,
                            title = "No received requests",
                            description = "When project leads or developers invite you to collaborate, requests will appear here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(currentReceived, key = { it.id }) { request ->
                                CyberReceivedCard(
                                    request = request,
                                    onAccept = {
                                        collaborationViewModel.acceptRequest(request.id)
                                        val idx = fallbackReceivedRequests.indexOfFirst { it.id == request.id }
                                        if (idx != -1) {
                                            fallbackReceivedRequests[idx] = fallbackReceivedRequests[idx].copy(status = CollaborationStatus.ACCEPTED)
                                        }
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Accepted invite from ${request.sender?.name ?: "developer"}!")
                                        }
                                    },
                                    onReject = {
                                        collaborationViewModel.rejectRequest(request.id)
                                        val idx = fallbackReceivedRequests.indexOfFirst { it.id == request.id }
                                        if (idx != -1) {
                                            fallbackReceivedRequests[idx] = fallbackReceivedRequests[idx].copy(status = CollaborationStatus.REJECTED)
                                        }
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Rejected request.")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // SENT REQUESTS
                    val currentSent = when (val state = requestsState) {
                        is RequestsUiState.Success -> if (state.sent.isNotEmpty()) state.sent else fallbackSentRequests
                        else -> fallbackSentRequests
                    }

                    if (currentSent.isEmpty()) {
                        EmptyAlertState(
                            icon = Icons.Default.Send,
                            title = "No sent invitations",
                            description = "Invitations you send to developers from project discovery or matches will be tracked here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(currentSent, key = { it.id }) { request ->
                                CyberSentCard(
                                    request = request,
                                    onCancel = {
                                        collaborationViewModel.cancelRequest(request.id)
                                        fallbackSentRequests.removeIf { it.id == request.id }
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Invitation cancelled.")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // ACTIVITY & NOTIFICATIONS
                    val currentActivities = when (val state = notificationsUiState) {
                        is NotificationsUiState.Success -> if (state.notifications.isNotEmpty()) state.notifications else fallbackActivities
                        else -> fallbackActivities
                    }

                    if (currentActivities.isEmpty()) {
                        EmptyAlertState(
                            icon = Icons.Default.Notifications,
                            title = "No activity yet",
                            description = "Charms earned, roadmap milestones, and repository updates will show here."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(currentActivities, key = { it.id }) { activity ->
                                CyberActivityCard(
                                    activity = activity,
                                    onClick = {
                                        if (!activity.read) {
                                            notificationsViewModel.markAsRead(activity.id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberReceivedCard(
    request: CollaborationRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val senderName = request.sender?.name?.ifBlank { null } ?: request.sender?.username ?: "Collaborator"
    val projectName = request.project?.name?.ifBlank { null } ?: "Open Source Project"
    val isPending = request.status == CollaborationStatus.PENDING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = BrutalSurfaceDark,
                borderColor = BrutalBorderWhite,
                borderWidth = 2.dp,
                shadowColor = PrimaryIndigo,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(name = senderName, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Project: $projectName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = ElectricMint,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Status sticker chip
                val statusBg = when (request.status) {
                    CollaborationStatus.ACCEPTED -> ElectricMint
                    CollaborationStatus.REJECTED -> Color(0xFFEF4444)
                    else -> AmberGold
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = BrutalInkBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BrutalSurfaceRaised)
                        .border(1.dp, BrutalBorderWhite.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ROLE: ${request.proposedRole}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = request.createdAt,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                )
            }

            if (request.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrutalCanvasDark)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\"${request.message}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reject
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = BrutalSurfaceRaised,
                                borderColor = BrutalBorderWhite,
                                borderWidth = 1.5.dp,
                                shadowColor = BrutalShadowBlack,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = onReject
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DECLINE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

                    // Accept
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = ElectricMint,
                                borderColor = BrutalShadowBlack,
                                borderWidth = 2.dp,
                                shadowColor = BrutalShadowBlack,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = onAccept
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = BrutalInkBlack, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACCEPT INVITE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = BrutalInkBlack,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberSentCard(
    request: CollaborationRequest,
    onCancel: () -> Unit
) {
    val receiverName = request.receiver?.name?.ifBlank { null } ?: request.receiver?.username ?: "Developer"
    val projectName = request.project?.name?.ifBlank { null } ?: "Project"
    val isPending = request.status == CollaborationStatus.PENDING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = BrutalSurfaceDark,
                borderColor = BrutalBorderWhite,
                borderWidth = 2.dp,
                shadowColor = BrutalShadowBlack,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(name = receiverName, modifier = Modifier.size(42.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sent to: $receiverName",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Project: $projectName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = ElectricMint,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPending) AmberGold else ElectricMint)
                        .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = request.status.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = BrutalInkBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Target Role: ${request.proposedRole} • Sent ${request.createdAt}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8)
                )
            )

            if (isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .tactileBrutalCard(
                            cornerRadius = 10.dp,
                            containerColor = BrutalSurfaceRaised,
                            borderColor = Color(0xFFF87171),
                            borderWidth = 1.5.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp,
                            onClick = onCancel
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "CANCEL INVITATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CyberActivityCard(
    activity: NotificationItem,
    onClick: () -> Unit
) {
    val isCharms = activity.type.uppercase() == "CHARMS"
    val isMilestone = activity.type.uppercase() == "MILESTONE"
    val isGithub = activity.type.uppercase() == "GITHUB"

    val icon = when {
        isCharms -> Icons.Default.Star
        isMilestone -> Icons.Default.Flag
        isGithub -> Icons.Default.Code
        else -> Icons.Default.Notifications
    }

    val iconTint = when {
        isCharms -> AmberGold
        isMilestone -> Color(0xFF38BDF8)
        isGithub -> Color(0xFFA855F7)
        else -> ElectricMint
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tactileBrutalCard(
                cornerRadius = 14.dp,
                containerColor = BrutalSurfaceDark,
                borderColor = BrutalBorderWhite,
                borderWidth = 2.dp,
                shadowColor = iconTint,
                shadowOffsetX = 3.dp,
                shadowOffsetY = 3.dp,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .brutalCard(
                        cornerRadius = 10.dp,
                        containerColor = iconTint,
                        borderColor = BrutalShadowBlack,
                        borderWidth = 1.5.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 2.dp,
                        shadowOffsetY = 2.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrutalInkBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (!activity.read) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (!activity.read) Color.White else Color(0xFFCBD5E1)
                        )
                    )

                    if (!activity.read) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricMint)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.content,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.createdAt,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyAlertState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8)
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
