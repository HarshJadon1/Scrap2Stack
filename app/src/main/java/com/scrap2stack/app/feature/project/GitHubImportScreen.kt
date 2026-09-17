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
import com.scrap2stack.app.core.ui.components.Scrap2StackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubImportScreen(
    viewModel: GitHubImportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: (String) -> Unit
) {
    var repoUrl by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is GitHubImportState.Success) {
            val projectId = (uiState as GitHubImportState.Success).project.id
            onNavigateToAnalysis(projectId)
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Import Open Source Project",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter any public GitHub repository URL. ScrapAI will analyze its codebase, detect required skills, and create a revival roadmap.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = repoUrl,
                onValueChange = { repoUrl = it },
                label = { Text("GitHub Repository URL") },
                placeholder = { Text("https://github.com/owner/repository") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is GitHubImportState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is GitHubImportState.Error) {
                Text(
                    text = (uiState as GitHubImportState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (uiState is GitHubImportState.Loading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = (uiState as GitHubImportState.Loading).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Scrap2StackButton(
                    text = "Analyze & Import",
                    onClick = { viewModel.importRepository(repoUrl) },
                    enabled = repoUrl.isNotBlank()
                )
            }
        }
    }
}
