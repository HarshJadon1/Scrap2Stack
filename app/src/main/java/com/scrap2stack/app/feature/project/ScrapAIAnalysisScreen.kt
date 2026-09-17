package com.scrap2stack.app.feature.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.model.ProjectRisk
import com.scrap2stack.app.domain.model.RequiredSkillRecommendation
import com.scrap2stack.app.domain.model.RoadmapStep
import com.scrap2stack.app.ui.theme.StatusReviving

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapAIAnalysisScreen(
    projectId: String,
    viewModel: ScrapAIAnalysisViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRevivalScore: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadAnalysis(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScrapAI Code Audit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ScrapAIAnalysisState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            is ScrapAIAnalysisState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadAnalysis(projectId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is ScrapAIAnalysisState.Success -> {
                ScrapAIAnalysisContent(
                    analysis = state.analysis,
                    innerPadding = innerPadding,
                    onNavigateToRevivalScore = onNavigateToRevivalScore
                )
            }
        }
    }
}

@Composable
private fun ScrapAIAnalysisContent(
    analysis: ProjectAnalysis,
    innerPadding: PaddingValues,
    onNavigateToRevivalScore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // AI Score Header
        Scrap2StackCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${analysis.revivalScore}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Revival Potential",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = analysis.scoreExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Executive Summary
        SectionHeader(title = "AI Codebase Summary")
        Text(
            text = analysis.projectSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Required Skills Detected
        if (analysis.requiredSkills.isNotEmpty()) {
            SectionHeader(title = "Detected Skill Gaps")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                analysis.requiredSkills.forEach { item ->
                    SkillRequirementRow(item)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Risks
        if (analysis.risks.isNotEmpty()) {
            SectionHeader(title = "Risk Assessment")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                analysis.risks.forEach { risk ->
                    RiskRow(risk)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recommended Roadmap
        if (analysis.roadmap.isNotEmpty()) {
            SectionHeader(title = "AI Revival Plan")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                analysis.roadmap.forEach { step ->
                    RoadmapStepCard(step)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Scrap2StackButton(
            text = "View Revival Strategy",
            onClick = onNavigateToRevivalScore
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SkillRequirementRow(item: RequiredSkillRecommendation) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.skill, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            StatusChip(
                status = item.importance.name,
                color = if (item.importance.name == "CRITICAL") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RiskRow(risk: ProjectRisk) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = risk.risk,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = risk.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun RoadmapStepCard(step: RoadmapStep) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHASE ${step.step}: ${step.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusChip(status = step.priority.name, color = StatusReviving)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
