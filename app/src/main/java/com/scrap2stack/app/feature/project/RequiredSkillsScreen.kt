package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequiredSkillsScreen(
    projectId: String,
    viewModel: RequiredSkillsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMatches: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadSkills(projectId)
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "SKILLS // GAP ANALYSIS",
                title = "Required Skills",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
        ) {
            when (val state = uiState) {
                is RequiredSkillsState.Loading -> LoadingView()
                is RequiredSkillsState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadSkills(projectId) }
                )
                is RequiredSkillsState.Success -> RequiredSkillsContent(
                    analysis = state.analysis,
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
    onNavigateToMatches: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI Skill Gap Analysis",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            color = brutal.textPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Based on project code analysis, these technical skills are required to execute the revival roadmap successfully.",
            style = MaterialTheme.typography.bodyMedium,
            color = brutal.textMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (analysis.requiredSkills.isEmpty()) {
            EmptyStateView(
                title = "No specific skills required",
                description = "Generalist contributors can jump right in."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                analysis.requiredSkills.forEach { recommendation ->
                    Scrap2StackCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recommendation.skill,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = brutal.textPrimary
                            )
                            StatusChip(
                                status = recommendation.importance.name,
                                color = if (recommendation.importance.name == "CRITICAL") StatusAbandoned else PrimaryIndigo
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Scrap2StackButton(
            text = "FIND DEVELOPER MATCHES",
            onClick = onNavigateToMatches
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
