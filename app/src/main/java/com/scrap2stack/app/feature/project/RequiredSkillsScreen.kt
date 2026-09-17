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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.ProjectAnalysis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequiredSkillsScreen(
    projectId: String,
    viewModel: RequiredSkillsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMatches: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadSkills(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Required Skills") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is RequiredSkillsState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            is RequiredSkillsState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadSkills(projectId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is RequiredSkillsState.Success -> {
                RequiredSkillsContent(
                    analysis = state.analysis,
                    innerPadding = innerPadding,
                    onNavigateToMatches = onNavigateToMatches
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RequiredSkillsContent(
    analysis: ProjectAnalysis,
    innerPadding: PaddingValues,
    onNavigateToMatches: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI Skill Gap Analysis",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Based on project code analysis, these technical skills are required to execute the revival roadmap successfully.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (analysis.requiredSkills.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No specific skills required.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                analysis.requiredSkills.forEach { recommendation ->
                    Scrap2StackCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recommendation.skill,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            StatusChip(
                                status = recommendation.importance.name,
                                color = if (recommendation.importance.name == "CRITICAL") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Scrap2StackButton(
            text = "Find Developer Matches",
            onClick = onNavigateToMatches
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
