package com.scrap2stack.app.feature.discovery

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.ProjectCard

enum class SortOrder {
    REVIVAL_DESC, REVIVAL_ASC, NEWEST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToProjectDetails: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var sortOrder by remember { mutableStateOf(SortOrder.REVIVAL_DESC) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Discover Projects",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by tech, skill, or name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (selectedCategory != "ALL" || selectedStatus != "ALL") MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Filter Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "Web", "Mobile", "AI/ML", "Open Source", "DevOps").forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is DiscoveryUiState.Loading -> LoadingView()
                is DiscoveryUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadProjects() })
                is DiscoveryUiState.Empty -> EmptyStateView(modifier = Modifier.fillMaxSize())
                is DiscoveryUiState.Success -> {
                    var projects = state.projects.filter { project ->
                        val matchesQuery = searchQuery.isEmpty() ||
                            project.name.contains(searchQuery, ignoreCase = true) ||
                            project.description.contains(searchQuery, ignoreCase = true) ||
                            project.technologies.any { tech -> tech.contains(searchQuery, ignoreCase = true) } ||
                            project.requiredSkills.any { skill -> skill.contains(searchQuery, ignoreCase = true) }

                        val matchesCategory = selectedCategory == "ALL" || project.category.equals(selectedCategory, ignoreCase = true)
                        val matchesStatus = selectedStatus == "ALL" || project.status.name.equals(selectedStatus, ignoreCase = true)

                        matchesQuery && matchesCategory && matchesStatus
                    }

                    projects = when (sortOrder) {
                        SortOrder.REVIVAL_DESC -> projects.sortedByDescending { it.revivalScore }
                        SortOrder.REVIVAL_ASC -> projects.sortedBy { it.revivalScore }
                        SortOrder.NEWEST -> projects
                    }

                    if (projects.isEmpty()) {
                        EmptyStateView(
                            message = if (searchQuery.isNotEmpty() || selectedCategory != "ALL" || selectedStatus != "ALL") 
                                "No projects match your search query or filter criteria." 
                            else 
                                "No projects found.",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(projects, key = { it.id }) { project ->
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

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Filter & Sort Projects", style = MaterialTheme.typography.titleLarge)

                    Text("Sort By Revival Score", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = sortOrder == SortOrder.REVIVAL_DESC,
                            onClick = { sortOrder = SortOrder.REVIVAL_DESC },
                            label = { Text("Highest First") }
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.REVIVAL_ASC,
                            onClick = { sortOrder = SortOrder.REVIVAL_ASC },
                            label = { Text("Lowest First") }
                        )
                    }

                    Text("Status Filter", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "ABANDONED", "INCOMPLETE", "REVIVING", "COMPLETED").forEach { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                label = { Text(status) }
                            )
                        }
                    }

                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    modifier: Modifier = Modifier,
    message: String = "No projects found"
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Be the first to create or import a project!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
