package com.scrap2stack.app.feature.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.service.ScrapAIEngine
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.RevivalEmerald

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
                title = { Text("Revival Score Analytics") },
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
    val breakdown = remember(project) { ScrapAIEngine.calculateRevivalBreakdown(project) }
    val displayScore = if (project.revivalScore > 0) project.revivalScore else breakdown.overallScore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Main Circular Viability Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            CircularProgressIndicator(
                progress = { displayScore / 100f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 12.dp,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$displayScore%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Viability Index",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistical Percentile Rank Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = RevivalEmerald.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, RevivalEmerald.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = ElectricMint
                )
                Text(
                    text = "Top ${100 - breakdown.percentileRank}% Revival Candidate • ${breakdown.viabilityLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Multi-Factor Viability Model",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ScrapAI Scientifically Grounded Revival Viability Model (SRVM) evaluated architecture, spec density, market viability, and skill availability.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dimensional Factors Breakdown Card
        Scrap2StackCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                ScoreFactorRow(
                    title = "Codebase Architecture",
                    subtitle = "Repo scaffolding, modern patterns & modularity",
                    score = breakdown.architectureScore
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ScoreFactorRow(
                    title = "Documentation & Spec Density",
                    subtitle = "Problem statement entropy & clarity index",
                    score = breakdown.documentationScore
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ScoreFactorRow(
                    title = "Market Interest & Tech Vitality",
                    subtitle = "Ecosystem growth & framework longevity",
                    score = breakdown.marketRelevanceScore
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ScoreFactorRow(
                    title = "Skill Market Availability",
                    subtitle = "Developer supply & team capacity balance",
                    score = breakdown.skillAvailabilityScore
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Empirical Effort Estimation Card (Power-Law Model)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Forecasted Time-to-MVP",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Estimated ~${breakdown.estimatedEffortWeeks} weeks (80% CI: ${breakdown.effortConfidenceInterval.first} – ${breakdown.effortConfidenceInterval.second} wks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Scrap2StackButton(
            text = "View Skill Requirements",
            onClick = onNavigateToRequiredSkills
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ScoreFactorRow(title: String, subtitle: String, score: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round
        )
    }
}
