package com.scrap2stack.app.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.service.ScrapAIEngine
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevivalScoreScreen(
    projectId: String,
    viewModel: RevivalScoreViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRequiredSkills: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadRevivalScore(projectId)
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "SRVM // SCORE",
                title = "Revival Score Analytics",
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
                is RevivalScoreState.Loading -> LoadingView()
                is RevivalScoreState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadRevivalScore(projectId) }
                )
                is RevivalScoreState.Success -> RevivalScoreContent(
                    project = state.project,
                    onNavigateToRequiredSkills = onNavigateToRequiredSkills
                )
            }
        }
    }
}

@Composable
private fun RevivalScoreContent(
    project: Project,
    onNavigateToRequiredSkills: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val breakdown = remember(project) { ScrapAIEngine.calculateRevivalBreakdown(project) }
    val displayScore = if (project.revivalScore > 0) project.revivalScore else breakdown.overallScore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Main Circular Viability Gauge inside Stack Pop frame
        Box(
            modifier = Modifier
                .brutalCard(
                    cornerRadius = 20.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = ElectricMint,
                    shadowOffsetX = 5.dp,
                    shadowOffsetY = 5.dp
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                CircularProgressIndicator(
                    progress = { displayScore / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = ElectricMint,
                    strokeWidth = 12.dp,
                    trackColor = brutal.surfaceRaised,
                    strokeCap = StrokeCap.Butt
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$displayScore%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = brutal.accentOnSurface
                    )
                    Text(
                        text = "VIABILITY INDEX",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = brutal.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistical Percentile Rank Badge
        Row(
            modifier = Modifier
                .brutalCard(
                    cornerRadius = 12.dp,
                    containerColor = ElectricMint,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = brutal.shadow,
                    shadowOffsetX = 3.dp,
                    shadowOffsetY = 3.dp
                )
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = BrutalInkBlack
            )
            Text(
                text = "Top ${100 - breakdown.percentileRank}% Revival Candidate • ${breakdown.viabilityLabel}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                ),
                color = BrutalInkBlack
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Multi-Factor Viability Model",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = brutal.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ScrapAI Scientifically Grounded Revival Viability Model (SRVM) evaluated architecture, spec density, market viability, and skill availability.",
            style = MaterialTheme.typography.bodyMedium,
            color = brutal.textMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dimensional Factors Breakdown Card
        Scrap2StackCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                ScoreFactorRow(
                    title = "Codebase Architecture",
                    subtitle = "Repo scaffolding, modern patterns & modularity",
                    score = breakdown.architectureScore
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = brutal.border.copy(alpha = 0.25f))
                ScoreFactorRow(
                    title = "Documentation & Spec Density",
                    subtitle = "Problem statement entropy & clarity index",
                    score = breakdown.documentationScore
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = brutal.border.copy(alpha = 0.25f))
                ScoreFactorRow(
                    title = "Market Interest & Tech Vitality",
                    subtitle = "Ecosystem growth & framework longevity",
                    score = breakdown.marketRelevanceScore
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = brutal.border.copy(alpha = 0.25f))
                ScoreFactorRow(
                    title = "Skill Market Availability",
                    subtitle = "Developer supply & team capacity balance",
                    score = breakdown.skillAvailabilityScore
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Empirical Effort Estimation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 14.dp,
                    containerColor = brutal.surfaceRaised,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = PrimaryIndigo,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp
                )
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
                    tint = brutal.accentOnSurface
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Forecasted Time-to-MVP",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Estimated ~${breakdown.estimatedEffortWeeks} weeks (80% CI: ${breakdown.effortConfidenceInterval.first} – ${breakdown.effortConfidenceInterval.second} wks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Scrap2StackButton(
            text = "VIEW SKILL REQUIREMENTS",
            onClick = onNavigateToRequiredSkills
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ScoreFactorRow(title: String, subtitle: String, score: Int) {
    val brutal = MaterialTheme.brutalColors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black), color = brutal.textPrimary)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = brutal.textMuted
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                ),
                color = brutal.accentOnSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brutal.surfaceRaised)
                .border(1.dp, brutal.border, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((score / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(ElectricMint)
            )
        }
    }
}
