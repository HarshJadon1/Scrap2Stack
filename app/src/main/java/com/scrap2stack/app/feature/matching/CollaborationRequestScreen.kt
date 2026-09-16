package com.scrap2stack.app.feature.matching

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationRequestScreen(
    projectId: String,
    developerId: String,
    viewModel: CollaborationViewModel,
    onNavigateBack: () -> Unit,
    onRequestSent: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var message by remember { mutableStateOf("I think your experience would be a great fit for this project.") }

    LaunchedEffect(uiState) {
        if (uiState is CollaborationUiState.Success) {
            onRequestSent()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Developer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Send a collaboration request",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Collaboration Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )

                if (uiState is CollaborationUiState.Error) {
                    Text(
                        text = (uiState as CollaborationUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Scrap2StackButton(
                    text = "Send Invitation",
                    onClick = { viewModel.sendRequest(projectId, developerId, message) },
                    enabled = uiState !is CollaborationUiState.Loading
                )
                
                TextButton(onClick = onNavigateBack) {
                    Text("Cancel")
                }
            }

            if (uiState is CollaborationUiState.Loading) {
                LoadingView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
