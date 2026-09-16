package com.scrap2stack.app.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.data.remote.dto.ProjectDto
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.StatusCompleted
import com.scrap2stack.app.ui.theme.StatusReviving

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    viewModel: ProjectDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScrapAI: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToWorkspace: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
        viewModel.loadProjectAnalysis(projectId)
    }

    val isSaved = (uiState as? ProjectDetailsState.Success)?.isSaved ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ProjectDetailsState.Success) {
                        IconButton(onClick = { viewModel.toggleSave(projectId) }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Unsave Project" else "Save Project",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
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
            is ProjectDetailsState.Deleted -> {
                LaunchedEffect(Unit) {
                    onNavigateBack()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectDetailsContent(
    project: ProjectDto,
    analysis: ProjectAnalysis?,
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
                "ABANDONED" -> StatusAbandoned
                "REVIVING" -> StatusReviving
                "COMPLETED" -> StatusCompleted
                else -> Color.Gray
            }
            StatusChip(status = project.status, color = statusColor)
        }

        if (project.technologies.isNotEmpty()) {
            Text(
                text = project.technologies.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scores
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreCard(label = "Revival Potential", score = "${project.revivalScore}%", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Insights Section
        if (analysis != null && analysis.recommendations.isNotEmpty()) {
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

        if (!project.problem.isNull_or_blank_safe()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Why was it abandoned?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = project.problem ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

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
        if (project.requiredSkills.isNotEmpty()) {
            Text("Required Skills", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Row(
                modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                project.requiredSkills.forEach { skill ->
                    SkillChip(skill = skill)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Scrap2StackOutlinedButton(
            text = "View Developer Matches",
            onClick = onNavigateToMatches
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToWorkspace,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Terminal, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Project Workspace")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (!project.githubUrl.isNullOrBlank()) {
            val uriHandler = LocalUriHandler.current
            TextButton(
                onClick = {
                    val url = project.githubUrl
                    if (url.isNotBlank()) {
                        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            "https://$url"
                        } else {
                            url
                        }
                        try {
                            uriHandler.openUri(formattedUrl)
                        } catch (e: Exception) {
                            // Ignore error
                        }
                    }
                },
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

private fun String?.isNull_or_blank_safe(): Boolean {
    return this == null || this.trim().isEmpty()
}

@Composable
fun AIInsightsSection(analysis: ProjectAnalysis) {
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
        analysis.recommendations.take(3).forEach { step ->
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
