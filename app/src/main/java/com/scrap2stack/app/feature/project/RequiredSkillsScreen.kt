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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.ProjectAnalysis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequiredSkillsScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMatches: () -> Unit,
    viewModel: RequiredSkillsViewModel = viewModel()
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
                SkillsContent(
                    analysis = state.analysis,
                    innerPadding = innerPadding,
                    onNavigateToMatches = onNavigateToMatches
                )
            }
        }
    }
}

@Composable
private fun SkillsContent(
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
            text = "Skills Needed to Revive This Project",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        analysis.requiredSkills.forEach { skillReq ->
            SkillRequirementCard(
                skill = skillReq.skill,
                level = "INTERMEDIATE",
                importance = skillReq.importance.name,
                coverage = "Required"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Skill Coverage Summary",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Connect with developers on the platform who possess these required skills.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Scrap2StackButton(
            text = "Find Developer Matches",
            onClick = onNavigateToMatches
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SkillRequirementCard(
    skill: String,
    level: String,
    importance: String,
    coverage: String,
    why: String? = null
) {
    Scrap2StackCard(modifier = Modifier.padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = skill, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = "Level: $level • Importance: $importance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = coverage,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            if (why != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = why,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
