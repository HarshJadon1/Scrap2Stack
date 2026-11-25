package com.scrap2stack.app.feature.matching

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.DeveloperMatch
import com.scrap2stack.app.domain.service.ContributionVerificationEngine
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperMatchingScreen(
    projectId: String,
    viewModel: DeveloperMatchingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDeveloperProfile: (String) -> Unit,
    onNavigateToRequestCollaboration: (String) -> Unit,
    onNavigateToWorkspace: (String) -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadMatches(projectId)
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "SYNERGY // ENGINE",
                title = "Developer Matches",
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
                is MatchingUiState.Loading -> {
                    LoadingView()
                }
                is MatchingUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        EmptyStateView(
                            title = "No matches found",
                            description = "Try adding more project skills or wait for more developers to join.",
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Scrap2StackButton(
                            text = "🚀 LAUNCH PROJECT WORKSPACE",
                            onClick = { onNavigateToWorkspace(projectId) }
                        )
                    }
                }
                is MatchingUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadMatches(projectId) }
                    )
                }
                is MatchingUiState.Success -> {
                    MatchingList(
                        projectId = projectId,
                        matches = state.matches,
                        onNavigateToDeveloperProfile = onNavigateToDeveloperProfile,
                        onNavigateToRequestCollaboration = onNavigateToRequestCollaboration,
                        onNavigateToWorkspace = onNavigateToWorkspace
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MatchingList(
    projectId: String,
    matches: List<DeveloperMatch>,
    onNavigateToDeveloperProfile: (String) -> Unit,
    onNavigateToRequestCollaboration: (String) -> Unit,
    onNavigateToWorkspace: (String) -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scrap2Stack Matching Engine identified these developers as your best revival partners.",
                style = MaterialTheme.typography.bodyMedium,
                color = brutal.textMuted
            )
        }

        items(matches) { match ->
            DeveloperMatchCard(
                match = match,
                onClick = { onNavigateToDeveloperProfile(match.developer.id) },
                onRequestCollaboration = { onNavigateToRequestCollaboration(match.developer.id) }
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .brutalCard(
                        cornerRadius = 16.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = PrimaryIndigo,
                        shadowOffsetX = 4.dp,
                        shadowOffsetY = 4.dp
                    )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ready to start building?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Head to the Project Workspace to collaborate, manage tasks, and track GitHub activity.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = brutal.textMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Scrap2StackButton(
                        text = "🚀 LAUNCH PROJECT WORKSPACE",
                        onClick = { onNavigateToWorkspace(projectId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperMatchCard(
    match: DeveloperMatch,
    onClick: () -> Unit,
    onRequestCollaboration: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val badgeBg = getMatchColor(match.score)
    val badgeText = if (match.score >= 80 && match.score < 92) Color.White else BrutalInkBlack

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(name = match.developer.name, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.developer.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                    Text(
                        text = match.developer.experienceLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = brutal.textMuted
                    )
                }

                BrutalBadge(
                    text = "${match.score}% MATCH",
                    containerColor = badgeBg,
                    contentColor = badgeText,
                    borderColor = brutal.border
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (match.matchedSkills.isNotEmpty()) {
                BrutalHeaderTag(text = "MATCHED SKILLS")
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    match.matchedSkills.forEach { skill ->
                        val verifiedMetric = ContributionVerificationEngine.getSkillConfidence(match.developer, skill)
                        SkillChip(skill = skill, isVerified = verifiedMetric.isVerified)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .brutalCard(
                        cornerRadius = 12.dp,
                        containerColor = brutal.surfaceRaised,
                        borderColor = brutal.border,
                        borderWidth = 1.5.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 2.dp,
                        shadowOffsetY = 2.dp
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = brutal.accentOnSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = match.compatibilityLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            ),
                            color = brutal.accentOnSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .tactileBrutalCard(
                            cornerRadius = 11.dp,
                            containerColor = brutal.surfaceRaised,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = brutal.shadow,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp,
                            onClick = onClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "View Profile",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .tactileBrutalCard(
                            cornerRadius = 11.dp,
                            containerColor = ElectricMint,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp,
                            onClick = onRequestCollaboration
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "INVITE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = BrutalInkBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun getMatchColor(score: Int): Color {
    return when {
        score >= 92 -> ElectricMint
        score >= 80 -> PrimaryIndigo
        score >= 65 -> AmberGold
        score >= 50 -> AmberGold
        else -> Color(0xFFCBD5E1)
    }
}
