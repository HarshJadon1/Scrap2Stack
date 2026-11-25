package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.BrutalTopBar
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
fun GitHubImportScreen(
    viewModel: GitHubImportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAnalysis: (String) -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    var repoUrl by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is GitHubImportState.Success) {
            val projectId = (uiState as GitHubImportState.Success).project.id
            onNavigateToAnalysis(projectId)
        }
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "IMPORT // GITHUB",
                title = "Import from GitHub",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .brutalCard(
                        cornerRadius = 18.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 5.dp,
                        shadowOffsetY = 5.dp
                    )
                    .padding(22.dp),
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
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = BrutalInkBlack
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Import Open Source Project",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = brutal.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter any public GitHub repository URL. ScrapAI will analyze its codebase, detect required skills, and create a revival roadmap.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = brutal.textMuted
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = { Text("GitHub Repository URL") },
                    placeholder = { Text("https://github.com/owner/repository") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = brutalTextFieldColors(),
                    enabled = uiState !is GitHubImportState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is GitHubImportState.Error) {
                    Text(
                        text = (uiState as GitHubImportState.Error).message,
                        color = StatusAbandoned,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (uiState is GitHubImportState.Loading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = brutal.accentOnSurface)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = (uiState as GitHubImportState.Loading).message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = brutal.accentOnSurface
                        )
                    }
                } else {
                    Scrap2StackButton(
                        text = "ANALYZE & IMPORT",
                        onClick = { viewModel.importRepository(repoUrl) },
                        enabled = repoUrl.isNotBlank()
                    )
                }
            }
        }
    }
}
