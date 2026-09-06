package com.scrap2stack.app.feature.matching

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.data.remote.dto.CollaborationRequestDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationRequestsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CollaborationRequestsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Received", "Sent")

    LaunchedEffect(Unit) {
        viewModel.loadRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collaboration Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (val state = uiState) {
                is CollaborationRequestsState.Loading -> {
                    LoadingView()
                }
                is CollaborationRequestsState.Empty -> {
                    EmptyStateView(
                        title = "No requests found",
                        description = "When you receive project invites or send them, they will appear here.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is CollaborationRequestsState.Error -> {
                    ErrorView(message = state.message, onRetry = { viewModel.loadRequests() })
                }
                is CollaborationRequestsState.Success -> {
                    val requests = if (selectedTab == 0) state.requests else emptyList() // Simplified for now
                    
                    if (requests.isEmpty() && selectedTab == 0) {
                        EmptyStateView(title = "No received requests", description = "You haven't received any invitations yet.", modifier = Modifier.fillMaxSize())
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                        ) {
                            items(requests) { request ->
                                ReceivedRequestCard(
                                    request = request,
                                    onAccept = { viewModel.acceptRequest(request.id) },
                                    onReject = { viewModel.rejectRequest(request.id) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ReceivedRequestCard(
    request: CollaborationRequestDto,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Scrap2StackCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = request.sender?.name ?: "User", modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = request.sender?.name ?: "Unknown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(text = "Invited you to: ${request.project?.name ?: "Project"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Role: ${request.proposedRole}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (request.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = request.message, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Reject")
                }
                Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                    Text("Accept")
                }
            }
        }
    }
}
