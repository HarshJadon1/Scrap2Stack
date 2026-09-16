package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.ProjectCard
import com.scrap2stack.app.core.ui.components.SectionHeader

@Composable
fun MyProjectsScreen(
    viewModel: MyProjectsViewModel,
    onNavigateToProjectDetails: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Created", "Joined", "Completed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        SectionHeader(title = "My Projects")

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is MyProjectsUiState.Loading -> LoadingView()
                is MyProjectsUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadMyProjects() })
                is MyProjectsUiState.Success -> {
                    val currentProjects = when (selectedTab) {
                        0 -> state.data.created
                        1 -> state.data.joined
                        2 -> state.data.completed
                        else -> emptyList()
                    }

                    if (currentProjects.isEmpty()) {
                        EmptyProjectsView(
                            message = when (selectedTab) {
                                0 -> "You haven't created any projects yet."
                                1 -> "You haven't joined any projects yet."
                                2 -> "No completed projects found."
                                else -> "No projects found."
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
