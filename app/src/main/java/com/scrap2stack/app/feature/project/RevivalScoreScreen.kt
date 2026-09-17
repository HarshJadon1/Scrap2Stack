package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevivalScoreScreen(
    projectId: String,
    viewModel: RevivalScoreViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRequiredSkills: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadRevivalScore(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revival Score") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is RevivalScoreState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            is RevivalScoreState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadRevivalScore(projectId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RevivalScoreState.Success -> {
                RevivalScoreContent(
                    project = state.project,
                    innerPadding = innerPadding,
                    onNavigateToRequiredSkills = onNavigateToRequiredSkills
                )
            }
        }
    }
}

@Composable
private fun RevivalScoreContent(
    project: Project,
    innerPadding: PaddingValues,
    onNavigateToRequiredSkills: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            CircularProgressIndicator(
                progress = { project.revivalScore / 100f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${project.revivalScore}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Viability",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Project Revival Assessment",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ScrapAI evaluated codebase completeness, documentation clarity, tech stack popularity, and team requirements.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Scrap2StackCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                ScoreFactorRow("Codebase Architecture", 85)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                ScoreFactorRow("Documentation & Specs", 60)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                ScoreFactorRow("Market Interest & Relevance", 90)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                ScoreFactorRow("Skill Availability", 75)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Scrap2StackButton(
            text = "View Skill Requirements",
            onClick = onNavigateToRequiredSkills
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ScoreFactorRow(title: String, score: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "$score/100",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
