package com.scrap2stack.app.feature.matching

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.data.remote.dto.MatchResultDto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeveloperMatchingScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDeveloperProfile: (String) -> Unit,
    onNavigateToRequestCollaboration: (String) -> Unit,
    viewModel: DeveloperMatchingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadMatches(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Best Developer Matches") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is MatchingState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            is MatchingState.Empty -> {
                EmptyStateView(
                    title = "No matches found",
                    description = "Try adding more project skills or wait for more developers to join.",
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is MatchingState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadMatches(projectId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is MatchingState.Success -> {
                MatchingList(
                    matches = state.matches,
                    innerPadding = innerPadding,
                    onNavigateToDeveloperProfile = onNavigateToDeveloperProfile,
                    onNavigateToRequestCollaboration = onNavigateToRequestCollaboration
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun MatchingList(
    matches: List<MatchResultDto>,
    innerPadding: PaddingValues,
    onNavigateToDeveloperProfile: (String) -> Unit,
    onNavigateToRequestCollaboration: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "People whose skills fit this project.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperMatchCard(
    match: MatchResultDto,
    onClick: () -> Unit,
    onRequestCollaboration: () -> Unit
) {
    Scrap2StackCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(name = match.developer.name, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.developer.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = match.developer.experienceLevel ?: "Intermediate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = getMatchColor(match.matchPercentage).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${match.matchPercentage}% Match",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = getMatchColor(match.matchPercentage)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (match.matchedSkills.isNotEmpty()) {
                Text(
                    text = "Matched Skills",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FlowRow(modifier = Modifier.padding(top = 4.dp)) {
                    match.matchedSkills.forEach { skill ->
                        SkillChip(skill = skill)
                    }
                }
            }

            if (match.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        match.reasons.forEach { reason ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("View Profile")
                }
                Button(
                    onClick = onRequestCollaboration,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Invite")
                }
            }
        }
    }
}

@Composable
private fun getMatchColor(percentage: Int): Color {
    return when {
        percentage >= 90 -> Color(0xFF2E7D32) // Excellent
        percentage >= 70 -> Color(0xFF1976D2) // Good
        percentage >= 50 -> Color(0xFFFBC02D) // Potential
        else -> Color.Gray
    }
}
