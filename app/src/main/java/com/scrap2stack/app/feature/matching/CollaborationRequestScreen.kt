package com.scrap2stack.app.feature.matching

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.BrutalTopBar
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.brutalTextFieldColors
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationRequestScreen(
    projectId: String,
    developerId: String,
    viewModel: CollaborationViewModel,
    onNavigateBack: () -> Unit,
    onRequestSent: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val uiState by viewModel.uiState.collectAsState()
    var message by remember { mutableStateOf("I think your experience would be a great fit for this project.") }

    LaunchedEffect(uiState) {
        if (uiState is CollaborationUiState.Success) {
            onRequestSent()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "TEAM // INVITE",
                title = "Invite Developer",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .brutalCard(
                            cornerRadius = 16.dp,
                            containerColor = ElectricMint,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 4.dp,
                            shadowOffsetY = 4.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = BrutalInkBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Send Collaboration Request",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    ),
                    color = brutal.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Personalize your invitation to let the developer know why they'd be a great match for this stack.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = brutal.textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { if (it.length <= 500) message = it },
                    label = { Text("Collaboration Message") },
                    supportingText = {
                        Text(
                            text = "${message.length} / 500",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            color = brutal.textMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    colors = brutalTextFieldColors()
                )

                if (uiState is CollaborationUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brutalCard(
                                cornerRadius = 12.dp,
                                containerColor = StatusAbandoned.copy(alpha = 0.12f),
                                borderColor = StatusAbandoned,
                                borderWidth = 2.dp,
                                shadowColor = brutal.shadow,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StatusAbandoned,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = (uiState as CollaborationUiState.Error).message,
                                color = StatusAbandoned,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Scrap2StackButton(
                    text = "SEND INVITATION",
                    onClick = { viewModel.sendRequest(projectId, developerId, message.trim()) },
                    enabled = uiState !is CollaborationUiState.Loading && message.isNotBlank()
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onNavigateBack) {
                    Text("Cancel", color = brutal.textMuted, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState is CollaborationUiState.Loading) {
                LoadingView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
