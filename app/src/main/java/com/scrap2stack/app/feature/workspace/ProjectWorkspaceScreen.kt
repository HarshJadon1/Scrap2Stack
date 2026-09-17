package com.scrap2stack.app.feature.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
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
import com.scrap2stack.app.data.repository.ChatRepositoryImpl
import com.scrap2stack.app.data.repository.TeamRepositoryImpl
import com.scrap2stack.app.data.repository.UserRepositoryImpl
import com.scrap2stack.app.domain.usecase.GetProjectMembersUseCase
import com.scrap2stack.app.feature.chat.ChatScreen
import com.scrap2stack.app.feature.chat.ChatViewModel
import com.scrap2stack.app.feature.chat.ChatViewModelFactory
import com.scrap2stack.app.feature.project.TeamScreen
import com.scrap2stack.app.feature.project.TeamViewModel
import com.scrap2stack.app.feature.project.TeamViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWorkspaceScreen(
    projectId: String,
    viewModel: WorkspaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMatches: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf(
        WorkspaceTab("Dashboard", Icons.Default.Dashboard),
        WorkspaceTab("Tasks", Icons.AutoMirrored.Filled.List),
        WorkspaceTab("Roadmap", Icons.Default.Map),
        WorkspaceTab("Chat", Icons.AutoMirrored.Filled.Comment),
        WorkspaceTab("GitHub", Icons.Default.Code),
        WorkspaceTab("Team", Icons.Default.Group)
    )

    LaunchedEffect(projectId) {
        viewModel.loadWorkspaceData(projectId)
        viewModel.subscribeToRealtimeWorkspace(projectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Project Workspace", style = MaterialTheme.typography.titleMedium)
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
                        0 -> WorkspaceDashboard(
                            tasksCount = state.tasks.size,
                            completedTasksCount = state.tasks.count { it.status.name == "COMPLETED" },
                            membersCount = state.members.size,
                            onNavigateToMatches = onNavigateToMatches
                        )
                        1 -> TasksScreen(
                            projectId = projectId,
                            tasks = state.tasks,
                            onCreateTask = { title, skill, priority ->
                                viewModel.createNewTask(title, skill, priority, projectId)
                            },
                            onStatusUpdate = { taskId, status -> 
                                viewModel.updateTaskStatus(taskId, status, projectId)
                            }
                        )
                        2 -> RoadmapScreen(
                            projectId = projectId,
                            roadmapItems = state.roadmapItems
                        )
                        3 -> ChatView(projectId = projectId)
                        4 -> GithubActivityScreen(
                            projectId = projectId
                        )
                        5 -> TeamView(projectId = projectId, onInviteDeveloper = onNavigateToMatches)
                    }
                }
            }
        }
    }
}

data class WorkspaceTab(val title: String, val icon: ImageVector)

@Composable
fun ChatView(projectId: String) {
    val chatRepository = remember { ChatRepositoryImpl() }
    val userRepository = remember { UserRepositoryImpl() }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatRepository, userRepository)
    )
    ChatScreen(
        projectId = projectId,
        viewModel = chatViewModel
    )
}

@Composable
fun TeamView(
    projectId: String,
    onInviteDeveloper: () -> Unit
) {
    val teamRepository = remember { TeamRepositoryImpl() }
    val getProjectMembersUseCase = remember { GetProjectMembersUseCase(teamRepository) }
    val teamViewModel: TeamViewModel = viewModel(
        factory = TeamViewModelFactory(getProjectMembersUseCase)
    )
    TeamScreen(
        projectId = projectId,
        viewModel = teamViewModel,
        onNavigateBack = {},
        onInviteDeveloper = onInviteDeveloper
    )
}
