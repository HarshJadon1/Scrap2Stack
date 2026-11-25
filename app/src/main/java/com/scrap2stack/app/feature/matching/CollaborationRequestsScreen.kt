package com.scrap2stack.app.feature.matching

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.CollaborationRequest
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationRequestsScreen(
    viewModel: CollaborationViewModel,
    onNavigateBack: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val requestsState by viewModel.requestsState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Received", "Sent")

    LaunchedEffect(selectedTab) {
        viewModel.loadRequests()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CollaborationUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is CollaborationUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = brutal.canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BrutalTopBar(
                tag = "INBOX // REQUESTS",
                title = "Collaboration Requests",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .dotGridBackground()
        ) {
            // Stack Pop Keycap Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tactileBrutalCard(
                                cornerRadius = 12.dp,
                                containerColor = if (isSelected) ElectricMint else brutal.surface,
                                borderColor = brutal.border,
                                borderWidth = 2.dp,
                                shadowColor = if (isSelected) BrutalShadowBlack else brutal.shadow,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = { selectedTab = index }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            ),
                            color = if (isSelected) BrutalInkBlack else brutal.textPrimary
                        )
                    }
                }
            }

            when (val state = requestsState) {
                is RequestsUiState.Loading -> {
                    LoadingView()
                }
                is RequestsUiState.Error -> {
                    ErrorView(message = state.message, onRetry = { viewModel.loadRequests() })
                }
                is RequestsUiState.Success -> {
                    val requests = if (selectedTab == 0) state.received else state.sent

                    if (requests.isEmpty()) {
                        EmptyStateView(
                            title = if (selectedTab == 0) "No received requests" else "No sent requests",
                            description = if (selectedTab == 0) "You haven't received any invitations yet." else "You haven't sent any invitations yet.",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
                        ) {
                            items(requests) { request ->
                                if (selectedTab == 0) {
                                    ReceivedRequestCard(
                                        request = request,
                                        onAccept = { viewModel.acceptRequest(request.id) },
                                        onReject = { viewModel.rejectRequest(request.id) }
                                    )
                                } else {
                                    SentRequestCard(
                                        request = request,
                                        onCancel = { viewModel.cancelRequest(request.id) }
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
fun ReceivedRequestCard(
    request: CollaborationRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val senderName = request.sender?.name?.ifBlank { null } ?: request.sender?.username ?: "Developer"
    val projectName = request.project?.name?.ifBlank { null } ?: "Project"

    Scrap2StackCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = senderName, modifier = Modifier.size(42.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                    Text(
                        text = "Invited you to join: $projectName",
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.accentOnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BrutalBadge(
                text = "ROLE: ${request.proposedRole.uppercase()}",
                containerColor = brutal.surfaceRaised,
                contentColor = brutal.textPrimary,
                borderColor = brutal.border
            )

            if (request.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = request.message, style = MaterialTheme.typography.bodyMedium, color = brutal.textPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (request.status.name == "PENDING") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .tactileBrutalCard(
                                cornerRadius = 11.dp,
                                containerColor = brutal.surfaceRaised,
                                borderColor = StatusAbandoned,
                                borderWidth = 2.dp,
                                shadowColor = BrutalShadowBlack,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = onReject
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "REJECT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            ),
                            color = StatusAbandoned
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .tactileBrutalCard(
                                cornerRadius = 11.dp,
                                containerColor = ElectricMint,
                                borderColor = brutal.border,
                                borderWidth = 2.dp,
                                shadowColor = BrutalShadowBlack,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = onAccept
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "ACCEPT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            ),
                            color = BrutalInkBlack
                        )
                    }
                }
            } else {
                BrutalBadge(
                    text = "STATUS: ${request.status.name}",
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    borderColor = brutal.border
                )
            }
        }
    }
}

@Composable
fun SentRequestCard(
    request: CollaborationRequest,
    onCancel: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val receiverName = request.receiver?.name?.ifBlank { null } ?: request.receiver?.username ?: "Developer"
    val projectName = request.project?.name?.ifBlank { null } ?: "Project"

    Scrap2StackCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = receiverName, modifier = Modifier.size(42.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sent to: $receiverName",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                    Text(
                        text = "Project: $projectName",
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.accentOnSurface
                    )
                }
                BrutalBadge(
                    text = request.status.name,
                    containerColor = brutal.surfaceRaised,
                    contentColor = brutal.textPrimary,
                    borderColor = brutal.border
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            BrutalBadge(
                text = "ROLE: ${request.proposedRole.uppercase()}",
                containerColor = brutal.surfaceRaised,
                contentColor = brutal.textMuted,
                borderColor = brutal.border
            )

            if (request.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = request.message, style = MaterialTheme.typography.bodyMedium, color = brutal.textPrimary)
            }

            if (request.status.name == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .tactileBrutalCard(
                            cornerRadius = 11.dp,
                            containerColor = brutal.surfaceRaised,
                            borderColor = StatusAbandoned,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp,
                            onClick = onCancel
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "CANCEL REQUEST",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = StatusAbandoned
                    )
                }
            }
        }
    }
}
