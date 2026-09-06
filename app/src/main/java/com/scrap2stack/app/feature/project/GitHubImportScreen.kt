package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.Scrap2StackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubImportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: (String) -> Unit,
    viewModel: GitHubImportViewModel = viewModel()
) {
    var repoUrl by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is GitHubImportState.Success) {
            val projectId = (uiState as GitHubImportState.Success).data.project.id
            onNavigateToAnalysis(projectId)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from GitHub") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Analyze Repository",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ScrapAI will analyze the repository to identify technologies, required skills, project health and revival potential.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = repoUrl,
                onValueChange = { repoUrl = it },
                label = { Text("Repository URL") },
                placeholder = { Text("https://github.com/user/project") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is GitHubImportState.Loading,
                isError = uiState is GitHubImportState.Error
            )

            if (uiState is GitHubImportState.Error) {
                Text(
                    text = (uiState as GitHubImportState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is GitHubImportState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message, style = MaterialTheme.typography.bodyMedium)
                }
                else -> {
                    Scrap2StackButton(
                        text = "Analyze Repository",
                        onClick = { viewModel.importRepository(repoUrl) },
                        enabled = repoUrl.startsWith("https://github.com/")
                    )
                }
            }
        }
    }
}
