package com.scrap2stack.app.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.data.remote.dto.ProjectDto
import com.scrap2stack.app.data.remote.dto.AnalysisDto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScrapAI: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToWorkspace: () -> Unit,
    viewModel: ProjectDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
        viewModel.loadProjectAnalysis(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ProjectDetailsState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            is ProjectDetailsState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadProjectDetails(projectId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is ProjectDetailsState.Success -> {
                ProjectDetailsContent(
                    project = state.project,
                    analysis = (analysisState as? ProjectAnalysisState.Success)?.analysis,
                    innerPadding = innerPadding,
                    onNavigateToScrapAI = onNavigateToScrapAI,
                    onNavigateToMatches = onNavigateToMatches,
                    onNavigateToWorkspace = onNavigateToWorkspace
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProjectDetailsContent(
    project: ProjectDto,
    analysis: AnalysisDto?,
    innerPadding: PaddingValues,
    onNavigateToScrapAI: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToWorkspace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            
            val statusColor = when (project.status) {
                "ABANDONED" -> com.scrap2stack.app.ui.theme.StatusAbandoned
                "REVIVING" -> com.scrap2stack.app.ui.theme.StatusReviving
                "COMPLETED" -> com.scrap2stack.app.ui.theme.StatusCompleted
                else -> Color.Gray
            }
            StatusChip(status = project.status, color = statusColor)
        }

        Text(
            text = project.technologies.joinToString(" • "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scores
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreCard(label = "Revival Potential", score = "${project.revivalScore}%", modifier = Modifier.weight(1f))
            ScoreCard(label = "Quality Score", score = "${project.qualityScore}/100", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PHASE 7: AI Insights Section
        if (analysis != null && analysis.nextSteps.isNotEmpty()) {
            AIInsightsSection(analysis)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Overview
        Text("Overview", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(
            text = project.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Analysis Teaser
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            onClick = onNavigateToScrapAI
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("ScrapAI Deep Audit", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("View technical risks, required skills, and roadmap.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Required Skills
        Text("Required Skills", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        FlowRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            project.requiredSkills.forEach { skill ->
                SkillChip(skill = skill)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Scrap2StackButton(
            text = "Request to Collaborate",
            onClick = { /* TODO */ }
        )
        Scrap2StackOutlinedButton(
            text = "View Developer Matches",
            onClick = onNavigateToMatches
        )
        
        if (project.progress > 0) {
            TextButton(
                onClick = onNavigateToWorkspace,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Terminal, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Project Workspace")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        project.githubUrl?.let { url ->
            TextButton(
                onClick = { /* TODO: Open URL */ },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View on GitHub")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AIInsightsSection(analysis: AnalysisDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Psychology, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "ScrapAI recommends...", 
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        analysis.nextSteps.take(3).forEach { step ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null, 
                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = step, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
