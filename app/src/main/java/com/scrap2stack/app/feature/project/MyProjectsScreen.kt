package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.ProjectCard
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.data.local.MockData

@Composable
fun MyProjectsScreen(
    onNavigateToProjectDetails: (String) -> Unit
) {
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
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filteredProjects = when (selectedTab) {
                0 -> MockData.projects.take(2) // Created
                1 -> MockData.projects.drop(2) // Joined
                else -> emptyList() // Completed
            }

            if (filteredProjects.isEmpty()) {
                item {
                    Text(
                        text = "No projects here yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            } else {
                items(filteredProjects) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onNavigateToProjectDetails(project.id) }
                    )
                }
            }
        }
    }
}
