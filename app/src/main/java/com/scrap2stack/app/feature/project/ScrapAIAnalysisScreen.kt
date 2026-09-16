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
import androidx.lifecycle.viewmodel.compose.viewModel
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
    onNavigateBack: () -> Unit,
    onNavigateToRevivalScore: () -> Unit,
    viewModel: ScrapAIAnalysisViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadAnalysis(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScrapAI Analysis") },
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
                AnalysisContent(
                    analysis = state.analysis,
                    innerPadding = innerPadding,
                    onNavigateToRevivalScore = onNavigateToRevivalScore
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisContent(
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
        // Revival Recommendation Card
        RevivalRecommendationCard(
            recommendation = "REVIVE_NOW",
            explanation = analysis.scoreExplanation
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("PROJECT SUMMARY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(
            text = analysis.projectSummary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreCard(label = "REVIVAL SCORE", score = "${analysis.revivalScore}/100", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Required Skills
        Text("REQUIRED SKILLS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        analysis.requiredSkills.forEach { skill ->
            SkillRequirementItem(skill)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Technical Risks
        Text("TECHNICAL RISKS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        analysis.risks.forEach { risk ->
            RiskFactorItem(risk)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Roadmap
        Text("RECOMMENDED ROADMAP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        analysis.roadmap.forEach { step ->
            RoadmapStepItem(step)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Scrap2StackButton(
            text = "Full Revival Score Details",
            onClick = onNavigateToRevivalScore
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun RevivalRecommendationCard(recommendation: String, explanation: String) {
    val (color, icon, label) = when (recommendation) {
        "REVIVE_NOW" -> Triple(StatusReviving, Icons.Default.CheckCircle, "REVIVE NOW")
        "REVIVE_WITH_CAUTION" -> Triple(Color(0xFFFFA000), Icons.Default.Info, "REVIVE WITH CAUTION")
        "NEEDS_REWORK" -> Triple(Color(0xFFF44336), Icons.Default.Warning, "NEEDS REWORK")
        else -> Triple(Color.Gray, Icons.Default.Info, "NOT RECOMMENDED")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = explanation, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SkillRequirementItem(skill: RequiredSkillRecommendation) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = skill.skill, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = skill.importance.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RiskFactorItem(risk: ProjectRisk) {
    val color = when (risk.severity.name) {
        "HIGH", "CRITICAL" -> MaterialTheme.colorScheme.error
        "MEDIUM" -> Color(0xFFFFA000)
        else -> Color.Gray
    }
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = risk.risk, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = risk.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun RoadmapStepItem(step: RoadmapStep) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = step.step.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
            Box(modifier = Modifier.width(2.dp).weight(1f).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = step.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = step.priority.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black)
            Text(text = step.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
