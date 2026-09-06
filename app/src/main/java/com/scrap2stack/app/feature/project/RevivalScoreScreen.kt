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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.data.remote.dto.RevivalScoreDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevivalScoreScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRequiredSkills: () -> Unit,
    viewModel: RevivalScoreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadRevivalScore(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revival Score") },
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
                    data = state.data,
                    innerPadding = innerPadding,
                    onNavigateToRequiredSkills = onNavigateToRequiredSkills
                )
            }
        }
    }
}

@Composable
private fun RevivalScoreContent(
    data: RevivalScoreDto,
    innerPadding: PaddingValues,
    onNavigateToRequiredSkills: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "${data.score}/100",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        )
        
        Text(
            text = data.label,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Why this score?",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = data.explanation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        data.factors.forEach { factor ->
            ScoreFactorItem(factor.name, factor.score / 100f)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Scrap2StackButton(
            text = "See Required Skills",
            onClick = onNavigateToRequiredSkills
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ScoreFactorItem(label: String, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = if (progress > 0.7f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
