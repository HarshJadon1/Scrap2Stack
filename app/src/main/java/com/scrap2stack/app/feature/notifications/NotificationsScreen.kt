package com.scrap2stack.app.feature.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.NotificationItem
import com.scrap2stack.app.feature.matching.CollaborationViewModel
import com.scrap2stack.app.feature.matching.ReceivedRequestCard
import com.scrap2stack.app.feature.matching.RequestsUiState
import com.scrap2stack.app.feature.matching.SentRequestCard

@Composable
fun NotificationsScreen(
    notificationsViewModel: NotificationsViewModel,
    collaborationViewModel: CollaborationViewModel,
    onNavigateToRequests: () -> Unit
) {
    val notificationsUiState by notificationsViewModel.uiState.collectAsState()
    val requestsState by collaborationViewModel.requestsState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Requests", "Activity")

    LaunchedEffect(Unit) {
        notificationsViewModel.loadNotifications()
        collaborationViewModel.loadRequests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "Alerts & Activity")

            if (selectedTab == 1 && notificationsUiState is NotificationsUiState.Success &&
                (notificationsUiState as NotificationsUiState.Success).notifications.any { !it.read }
            ) {
                IconButton(onClick = { notificationsViewModel.markAllAsRead() }) {
                    Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read")
                }
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title)
                            if (index == 0 && requestsState is RequestsUiState.Success) {
                                val pendingCount = (requestsState as RequestsUiState.Success).received.count { it.status.name == "PENDING" }
                                if (pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge {
                                        Text(pendingCount.toString())
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedTab == 0) {
                // Requests Tab
                when (val state = requestsState) {
                    is RequestsUiState.Loading -> LoadingView()
                    is RequestsUiState.Error -> ErrorView(message = state.message, onRetry = { collaborationViewModel.loadRequests() })
                    is RequestsUiState.Success -> {
                        val allRequests = state.received + state.sent

                        if (allRequests.isEmpty()) {
                            EmptyStateView(
                                title = "No collaboration requests",
                                description = "When other developers invite you to projects or request to join yours, they will appear here.",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                if (state.received.isNotEmpty()) {
                                    item {
                                        Text("RECEIVED REQUESTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    items(state.received) { request ->
                                        ReceivedRequestCard(
                                            request = request,
                                            onAccept = { collaborationViewModel.acceptRequest(request.id) },
                                            onReject = { collaborationViewModel.rejectRequest(request.id) }
                                        )
                                    }
                                }

                                if (state.sent.isNotEmpty()) {
                                    item {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("SENT REQUESTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    items(state.sent) { request ->
                                        SentRequestCard(
                                            request = request,
                                            onCancel = { collaborationViewModel.cancelRequest(request.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Notifications / Activity Tab
                when (val state = notificationsUiState) {
                    is NotificationsUiState.Loading -> LoadingView()
                    is NotificationsUiState.Error -> ErrorView(message = state.message, onRetry = { notificationsViewModel.loadNotifications() })
                    is NotificationsUiState.Success -> {
                        if (state.notifications.isEmpty()) {
                            EmptyStateView(
                                title = "No system activity yet",
                                description = "Updates regarding your tasks, charms, and roadmap milestones will appear here.",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(state.notifications) { notification ->
                                    NotificationCard(
                                        notification = notification,
                                        onClick = {
                                            if (!notification.read) {
                                                notificationsViewModel.markAsRead(notification.id)
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

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Scrap2StackCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = if (!notification.read) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal
                    )
                )
                if (notification.content.isNotEmpty()) {
                    Text(
                        text = notification.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
