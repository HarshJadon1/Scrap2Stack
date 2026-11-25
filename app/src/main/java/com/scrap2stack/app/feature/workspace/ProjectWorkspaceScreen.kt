package com.scrap2stack.app.feature.workspace

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
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
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWorkspaceScreen(
    projectId: String,
    viewModel: WorkspaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMatches: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isGeneratingRoadmap by viewModel.isGeneratingRoadmap.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val brutal = MaterialTheme.brutalColors

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
        containerColor = brutal.canvas,
        topBar = {
            Surface(
                color = brutal.canvas
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = brutal.surfaceRaised,
                                borderColor = brutal.border,
                                borderWidth = 2.dp,
                                shadowColor = brutal.shadow,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = onNavigateBack
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brutal.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WORKSPACE //",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = brutal.accentOnSurface
                        )
                        Text(
                            text = (uiState as? WorkspaceState.Success)?.project?.name ?: "Project Workspace",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = brutal.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        bottomBar = {
            WorkspaceKeycapDock(
                tabs = tabs,
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .dotGridBackground()
        ) {
            when (val state = uiState) {
                is WorkspaceState.Loading -> LoadingView()
                is WorkspaceState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadWorkspaceData(projectId) })
                is WorkspaceState.Success -> {
                    when (selectedTab) {
                        0 -> WorkspaceDashboard(
                            project = state.project,
                            tasks = state.tasks,
                            tasksCount = state.tasks.size,
                            completedTasksCount = state.tasks.count { it.status.name == "COMPLETED" },
                            membersCount = state.members.size,
                            isShipped = state.isShipped,
                            onNavigateToMatches = onNavigateToMatches,
                            onShipProject = {
                                viewModel.shipProject(projectId)
                            }
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
                            roadmapItems = state.roadmapItems,
                            isGenerating = isGeneratingRoadmap,
                            isLiveData = state.isRoadmapLiveData,
                            statusNote = state.roadmapStatusNote,
                            onGenerateRoadmap = {
                                viewModel.generateAiRoadmap(projectId = projectId)
                            },
                            onStatusChange = { itemId, newStatus ->
                                viewModel.updateRoadmapStatus(itemId, newStatus, projectId)
                            },
                            onConvertPhaseToTasks = { phaseItem ->
                                viewModel.convertRoadmapPhaseToTasks(phaseItem, projectId)
                            }
                        )
                        3 -> ChatView(projectId = projectId)
                        4 -> GithubActivityScreen(
                            projectId = projectId,
                            repoUrl = state.project?.githubUrl?.ifBlank { null } ?: "https://github.com/Scrap2Stack/revival-engine"
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
        onNavigateBack = null,
        onInviteDeveloper = onInviteDeveloper
    )
}

@Composable
internal fun WorkspaceKeycapDock(
    tabs: List<WorkspaceTab>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brutal.canvas)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = brutal.shadow,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp
                )
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) ElectricMint else brutal.surfaceRaised,
                        label = "workspaceTabBg"
                    )
                    val contentColor = if (isSelected) BrutalInkBlack else brutal.textMuted
                    val liftOffset by animateDpAsState(
                        targetValue = if (isSelected) (-2).dp else 0.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "workspaceTabLift"
                    )

                    Row(
                        modifier = Modifier
                            .graphicsLayer { translationY = liftOffset.toPx() }
                            .tactileBrutalCard(
                                cornerRadius = 12.dp,
                                containerColor = containerColor,
                                borderColor = brutal.border,
                                borderWidth = if (isSelected) 2.dp else 1.5.dp,
                                shadowColor = if (isSelected) BrutalShadowBlack else brutal.shadow,
                                shadowOffsetX = if (isSelected) 3.dp else 2.dp,
                                shadowOffsetY = if (isSelected) 3.dp else 2.dp,
                                onClick = { onSelectTab(index) }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.4.sp
                            ),
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }
    }
}
