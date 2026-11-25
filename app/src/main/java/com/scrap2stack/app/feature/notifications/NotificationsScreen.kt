package com.scrap2stack.app.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.*
import com.scrap2stack.app.feature.matching.CollaborationUiState
import com.scrap2stack.app.feature.matching.CollaborationViewModel
import com.scrap2stack.app.feature.matching.RequestsUiState
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    notificationsViewModel: NotificationsViewModel,
    collaborationViewModel: CollaborationViewModel,
    onNavigateToRequests: () -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = brutal.canvas
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
                        color = brutal.accentOnSurface
                    )
                    Text(
                        text = "Alerts & Collabs",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = brutal.textPrimary
                        )
                    )
                    Text(
                        text = "Requests, invites, and realtime team activity",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = brutal.textMuted
                        )
                    )
                }

                if (selectedTab == 2) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = brutal.surfaceRaised,
                                borderColor = brutal.border,
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
                            tint = brutal.accentOnSurface,
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
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = brutal.shadow,
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
                        val badgeCount = when (index) {
                            0 -> (requestsState as? RequestsUiState.Success)?.received?.count { it.status == CollaborationStatus.PENDING } ?: 0
                            2 -> NotificationsViewModel.countUnread(notificationsUiState)
                            else -> 0
                        }

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
                                        color = if (isSelected) BrutalInkBlack else brutal.textMuted,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                )
                                if (badgeCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) BrutalInkBlack else ElectricMint),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (badgeCount > 9) "9+" else "$badgeCount",
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

            // Tab Content backed exclusively by real Supabase state
            when (selectedTab) {
                0 -> {
                    when (val state = requestsState) {
                        is RequestsUiState.Loading -> LoadingView()
                        is RequestsUiState.Error -> ErrorView(
                            message = state.message,
                            onRetry = { collaborationViewModel.loadRequests() }
                        )
                        is RequestsUiState.Success -> {
                            if (state.received.isEmpty()) {
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
                                    items(state.received, key = { it.id }) { request ->
                                        CyberReceivedCard(
                                            request = request,
                                            onAccept = {
                                                collaborationViewModel.acceptRequest(request.id)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Accepted invite from ${request.sender?.name ?: "developer"}!")
                                                }
                                            },
                                            onReject = {
                                                collaborationViewModel.rejectRequest(request.id)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Rejected request.")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    when (val state = requestsState) {
                        is RequestsUiState.Loading -> LoadingView()
                        is RequestsUiState.Error -> ErrorView(
                            message = state.message,
                            onRetry = { collaborationViewModel.loadRequests() }
                        )
                        is RequestsUiState.Success -> {
                            if (state.sent.isEmpty()) {
                                EmptyAlertState(
                                    icon = Icons.AutoMirrored.Filled.Send,
                                    title = "No sent invitations",
                                    description = "Invitations you send to developers from project discovery or matches will be tracked here."
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    contentPadding = PaddingValues(bottom = 32.dp)
                                ) {
                                    items(state.sent, key = { it.id }) { request ->
                                        CyberSentCard(
                                            request = request,
                                            onCancel = {
                                                collaborationViewModel.cancelRequest(request.id)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Invitation cancelled.")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    when (val state = notificationsUiState) {
                        is NotificationsUiState.Loading -> LoadingView()
                        is NotificationsUiState.Error -> ErrorView(
                            message = state.message,
                            onRetry = { notificationsViewModel.loadNotifications() }
                        )
                        is NotificationsUiState.Success -> {
                            if (state.notifications.isEmpty()) {
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
                                    items(state.notifications, key = { it.id }) { activity ->
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
    }
}

@Composable
fun CyberReceivedCard(
    request: CollaborationRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val senderName = request.sender?.name?.ifBlank { null } ?: request.sender?.username ?: "Collaborator"
    val projectName = request.project?.name?.ifBlank { null } ?: "Open Source Project"
    val isPending = request.status == CollaborationStatus.PENDING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
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
                            color = brutal.textPrimary
                        )
                    )
                    Text(
                        text = "Project: $projectName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = brutal.accentOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Status sticker chip
                val statusBg = when (request.status) {
                    CollaborationStatus.ACCEPTED -> ElectricMint
                    CollaborationStatus.REJECTED -> StatusAbandoned
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
                            color = if (request.status == CollaborationStatus.REJECTED) Color.White else BrutalInkBlack,
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
                        .background(brutal.surfaceRaised)
                        .border(1.5.dp, brutal.border, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ROLE: ${request.proposedRole}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = brutal.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = request.createdAt.take(10),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = brutal.textMuted,
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
                        .background(brutal.canvas)
                        .border(1.5.dp, brutal.border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "\"${request.message}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = brutal.textPrimary,
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = brutal.surfaceRaised,
                                borderColor = brutal.border,
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
                                color = brutal.textPrimary,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

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
    val brutal = MaterialTheme.brutalColors
    val receiverName = request.receiver?.name?.ifBlank { null } ?: request.receiver?.username ?: "Developer"
    val projectName = request.project?.name?.ifBlank { null } ?: "Project"
    val isPending = request.status == CollaborationStatus.PENDING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
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
                            color = brutal.textPrimary
                        )
                    )
                    Text(
                        text = "Project: $projectName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = brutal.accentOnSurface,
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
                text = "Target Role: ${request.proposedRole} • Sent ${request.createdAt.take(10)}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = brutal.textMuted
                )
            )

            if (isPending) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .tactileBrutalCard(
                            cornerRadius = 10.dp,
                            containerColor = brutal.surfaceRaised,
                            borderColor = StatusAbandoned,
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
                            color = StatusAbandoned,
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
    val brutal = MaterialTheme.brutalColors
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
                containerColor = brutal.surface,
                borderColor = brutal.border,
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
                            fontWeight = if (!activity.read) FontWeight.Black else FontWeight.SemiBold,
                            color = if (!activity.read) brutal.textPrimary else brutal.textMuted
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
                        color = brutal.textMuted,
                        lineHeight = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activity.createdAt.take(10),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = brutal.textMuted,
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
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = brutal.shadow,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .brutalCard(
                        cornerRadius = 16.dp,
                        containerColor = brutal.surfaceRaised,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = brutal.accentOnSurface,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = brutal.textPrimary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = brutal.textMuted
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
