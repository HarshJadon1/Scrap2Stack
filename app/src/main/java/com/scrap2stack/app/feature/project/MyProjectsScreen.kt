package com.scrap2stack.app.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.EmptyStateView
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.ProjectCard
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@Composable
fun MyProjectsScreen(
    viewModel: MyProjectsViewModel,
    onNavigateToProjectDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Created", "Joined", "Saved", "Completed")
    val brutal = MaterialTheme.brutalColors

    LaunchedEffect(Unit) {
        viewModel.loadMyProjects()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brutal.canvas)
            .dotGridBackground()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        SectionHeader(title = "My Projects")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .tactileBrutalCard(
                            cornerRadius = 10.dp,
                            containerColor = if (isSelected) ElectricMint else brutal.surfaceRaised,
                            borderColor = if (isSelected) BrutalShadowBlack else brutal.border,
                            borderWidth = 1.5.dp,
                            shadowColor = if (isSelected) PrimaryIndigo else brutal.shadow,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp,
                            onClick = { selectedTab = index }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) BrutalInkBlack else brutal.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is MyProjectsUiState.Loading -> LoadingView()
                is MyProjectsUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadMyProjects() })
                is MyProjectsUiState.Success -> {
                    val currentProjects = when (selectedTab) {
                        0 -> state.data.created
                        1 -> state.data.joined
                        2 -> state.data.saved
                        3 -> state.data.completed
                        else -> emptyList()
                    }

                    if (currentProjects.isEmpty()) {
                        EmptyProjectsView(
                            message = when (selectedTab) {
                                0 -> "You haven't created any projects yet."
                                1 -> "You haven't joined any projects yet."
                                2 -> "No saved projects yet. Tap the bookmark icon on any project details page to save it!"
                                3 -> "No completed projects found."
                                else -> "No projects found."
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(currentProjects) { project ->
                                ProjectCard(
                                    project = project,
                                    onClick = { onNavigateToProjectDetails(project.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProjectsView(message: String = "No projects found in this category.") {
    EmptyStateView(
        title = "Empty Stack",
        description = message
    )
}
