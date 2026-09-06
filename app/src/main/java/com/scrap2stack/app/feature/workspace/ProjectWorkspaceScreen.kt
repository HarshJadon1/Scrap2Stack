package com.scrap2stack.app.feature.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.feature.charms.CharmsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWorkspaceScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMatches: () -> Unit,
    viewModel: WorkspaceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf(
        WorkspaceTab("Dashboard", Icons.Default.Dashboard),
        WorkspaceTab("Tasks", Icons.AutoMirrored.Filled.List),
        WorkspaceTab("Roadmap", Icons.Default.Map),
        WorkspaceTab("GitHub", Icons.Default.Code),
        WorkspaceTab("Team", Icons.Default.Group),
        WorkspaceTab("Charms", Icons.Default.Stars)
    )

    LaunchedEffect(projectId) {
        viewModel.loadWorkspaceData(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Project Workspace", style = MaterialTheme.typography.titleMedium)
                        if (uiState is WorkspaceState.Success) {
                            Text((uiState as WorkspaceState.Success).workspace.name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is WorkspaceState.Loading -> LoadingView()
                is WorkspaceState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadWorkspaceData(projectId) })
                is WorkspaceState.Success -> {
                    when (selectedTab) {
                        0 -> WorkspaceDashboard(state.workspace, onNavigateToMatches = onNavigateToMatches)
                        1 -> TasksScreen(projectId, state.tasks, onStatusUpdate = { taskId, status -> 
                            viewModel.updateTaskStatus(taskId, status, projectId)
                        })
                        2 -> RoadmapScreen(projectId, state.roadmap)
                        3 -> GithubActivityScreen(projectId, contributions = state.contributions, onSync = { viewModel.syncGitHub(projectId) })
                        4 -> TeamView(projectId)
                        5 -> CharmsScreen(onNavigateBack = null)
                    }
                }
            }
        }
    }
}

data class WorkspaceTab(val title: String, val icon: ImageVector)

@Composable
fun TeamView(projectId: String) {
    com.scrap2stack.app.feature.project.TeamScreen(
        projectId = projectId,
        onNavigateBack = {},
        onInviteDeveloper = {}
    )
}
