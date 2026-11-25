package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.model.ProjectRisk
import com.scrap2stack.app.domain.model.RequiredSkillRecommendation
import com.scrap2stack.app.domain.model.RoadmapStep
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.StatusReviving
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapAIAnalysisScreen(
    projectId: String,
    viewModel: ScrapAIAnalysisViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRevivalScore: () -> Unit,
    onNavigateToWorkspace: (String) -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadAnalysis(projectId)
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "AUDIT // SCRAPAI",
                title = "ScrapAI Code Audit",
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
                is ScrapAIAnalysisState.Loading -> LoadingView()
                is ScrapAIAnalysisState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadAnalysis(projectId) }
                )
                is ScrapAIAnalysisState.Success -> ScrapAIAnalysisContent(
                    analysis = state.analysis,
                    onNavigateToRevivalScore = onNavigateToRevivalScore,
                    onNavigateToWorkspace = onNavigateToWorkspace
                )
            }
        }
    }
}

@Composable
private fun ScrapAIAnalysisContent(
    analysis: ProjectAnalysis,
    onNavigateToRevivalScore: () -> Unit,
    onNavigateToWorkspace: (String) -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // AI Score Header
        Scrap2StackCard(
            modifier = Modifier.fillMaxWidth(),
            shadowColor = ElectricMint
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .brutalCard(
                            cornerRadius = 14.dp,
                            containerColor = ElectricMint,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${analysis.revivalScore}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = BrutalInkBlack
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Revival Potential",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                    Text(
                        text = analysis.scoreExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Executive Summary
        SectionHeader(title = "AI Codebase Summary")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 14.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = brutal.shadow,
                    shadowOffsetX = 3.dp,
                    shadowOffsetY = 3.dp
                )
                .padding(16.dp)
        ) {
            Text(
                text = analysis.projectSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = brutal.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Required Skills Detected
        if (analysis.requiredSkills.isNotEmpty()) {
            SectionHeader(title = "Detected Skill Gaps")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                analysis.requiredSkills.forEach { item ->
                    SkillRequirementRow(item)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Risks
        if (analysis.risks.isNotEmpty()) {
            SectionHeader(title = "Risk Assessment")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                analysis.risks.forEach { risk ->
                    RiskRow(risk)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
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
            text = "🚀 ADOPT PLAN & LAUNCH WORKSPACE",
            onClick = { onNavigateToWorkspace(analysis.projectId) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Scrap2StackOutlinedButton(
            text = "📊 VIEW STRATEGY & MATCH TEAMMATES",
            onClick = onNavigateToRevivalScore
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SkillRequirementRow(item: RequiredSkillRecommendation) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 12.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 3.dp,
                shadowOffsetY = 3.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                item.skill,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                color = brutal.textPrimary
            )
            StatusChip(
                status = item.importance.name,
                color = if (item.importance.name == "CRITICAL") StatusAbandoned else PrimaryIndigo
            )
        }
    }
}

@Composable
private fun RiskRow(risk: ProjectRisk) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 12.dp,
                containerColor = StatusAbandoned.copy(alpha = 0.08f),
                borderColor = StatusAbandoned,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 3.dp,
                shadowOffsetY = 3.dp
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusAbandoned,
                modifier = Modifier.size(20.dp).padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = risk.risk,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = StatusAbandoned
                )
                Text(
                    text = risk.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = brutal.textPrimary
                )
            }
        }
    }
}

@Composable
private fun RoadmapStepCard(step: RoadmapStep) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 14.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = PrimaryIndigo,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHASE ${step.step}: ${step.title}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    ),
                    color = brutal.accentOnSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(status = step.priority.name, color = StatusReviving)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = brutal.textMuted
            )
        }
    }
}
