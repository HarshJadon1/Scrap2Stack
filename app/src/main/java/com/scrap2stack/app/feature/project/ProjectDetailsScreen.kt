package com.scrap2stack.app.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.domain.service.ScrapAIEngine
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.StatusCompleted
import com.scrap2stack.app.ui.theme.StatusReviving

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: String,
    viewModel: ProjectDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToScrapAI: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToWorkspace: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
        viewModel.loadProjectAnalysis(projectId)
    }

    val successState = uiState as? ProjectDetailsState.Success
    val isSaved = successState?.isSaved ?: false
    val isOwner = successState?.isOwner ?: false

    if (showDeleteDialog && successState != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Project?") },
            text = { Text("Are you sure you want to delete '${successState.project.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProject(projectId)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog && successState != null) {
        EditProjectDialog(
            project = successState.project,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedProject ->
                showEditDialog = false
                viewModel.updateProject(updatedProject)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (successState != null) {
                        IconButton(onClick = { viewModel.toggleSave(projectId) }) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Unsave Project" else "Save Project",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }

                        if (isOwner) {
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Project") },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                        onClick = {
                                            showMenu = false
                                            showEditDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Project", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMenu = false
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ProjectDetailsState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            is ProjectDetailsState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadProjectDetails(projectId) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is ProjectDetailsState.Success -> {
                ProjectDetailsContent(
                    project = state.project,
                    analysis = (analysisState as? ProjectAnalysisState.Success)?.analysis,
                    innerPadding = innerPadding,
                    onNavigateToScrapAI = onNavigateToScrapAI,
                    onNavigateToMatches = onNavigateToMatches,
                    onNavigateToWorkspace = onNavigateToWorkspace
                )
            }
            is ProjectDetailsState.Deleted -> {
                LaunchedEffect(Unit) {
                    onNavigateBack()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectDetailsContent(
    project: Project,
    analysis: ProjectAnalysis?,
    innerPadding: PaddingValues,
    onNavigateToScrapAI: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToWorkspace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            
            val statusColor = when (project.status.name) {
                "ABANDONED" -> StatusAbandoned
                "REVIVING" -> StatusReviving
                "COMPLETED" -> StatusCompleted
                else -> Color.Gray
            }
            StatusChip(status = project.status.name, color = statusColor)
        }

        if (project.technologies.isNotEmpty()) {
            Text(
                text = project.technologies.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayScore = if (project.revivalScore > 0) {
            project.revivalScore
        } else if (analysis != null && analysis.revivalScore > 0) {
            analysis.revivalScore
        } else {
            ScrapAIEngine.calculateRevivalScore(project)
        }

        // Scores
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreCard(label = "Revival Potential", score = "${displayScore}%", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Insights Section
        if (analysis != null && analysis.recommendations.isNotEmpty()) {
            AIInsightsSection(analysis)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Overview
        Text("Overview", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(
            text = project.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (project.problem.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Why was it abandoned?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = project.problem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Analysis Teaser
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            onClick = onNavigateToScrapAI
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("ScrapAI Deep Audit", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("View technical risks, required skills, and roadmap.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Required Skills
        if (project.requiredSkills.isNotEmpty()) {
            Text("Required Skills", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Row(
                modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                project.requiredSkills.forEach { skill ->
                    SkillChip(skill = skill)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Primary Workflow CTA: Open Workspace
        Scrap2StackButton(
            text = "🚀 Enter Project Workspace",
            onClick = onNavigateToWorkspace
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToMatches,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Find Matches", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onNavigateToScrapAI,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AI Roadmap", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        if (project.githubUrl.isNotBlank()) {
            val uriHandler = LocalUriHandler.current
            TextButton(
                onClick = {
                    val url = project.githubUrl
                    if (url.isNotBlank()) {
                        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            "https://$url"
                        } else {
                            url
                        }
                        try {
                            uriHandler.openUri(formattedUrl)
                        } catch (e: Exception) {
                            // Ignore error
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Code, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View on GitHub")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AIInsightsSection(analysis: ProjectAnalysis) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Psychology, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "ScrapAI recommends...", 
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        analysis.recommendations.take(3).forEach { step ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null, 
                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = step, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun EditProjectDialog(
    project: Project,
    onDismiss: () -> Unit,
    onConfirm: (Project) -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var description by remember { mutableStateOf(project.description) }
    var problem by remember { mutableStateOf(project.problem) }
    var category by remember { mutableStateOf(project.category) }
    var technologies by remember { mutableStateOf(project.technologies.joinToString(", ")) }
    var skills by remember { mutableStateOf(project.requiredSkills.joinToString(", ")) }
    var status by remember { mutableStateOf(project.status) }
    var githubUrl by remember { mutableStateOf(project.githubUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Project", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = problem,
                    onValueChange = { problem = it },
                    label = { Text("Why was it abandoned?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = technologies,
                    onValueChange = { technologies = it },
                    label = { Text("Technologies (Comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Required Skills (Comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.titleSmall)
                val categoryOptions = remember(project.category) {
                    (listOf("Web", "Mobile", "AI/ML", "DevOps", "Cloud", "Open Source") + project.category).distinct().filter { it.isNotBlank() }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryOptions.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Text("Project Status", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProjectStatus.entries.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.name, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = githubUrl,
                    onValueChange = { githubUrl = it },
                    label = { Text("GitHub URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val techList = technologies.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val skillList = skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val updated = project.copy(
                        name = name.ifBlank { project.name },
                        description = description,
                        problem = problem,
                        category = category,
                        technologies = techList,
                        requiredSkills = skillList,
                        status = status,
                        githubUrl = githubUrl
                    )
                    onConfirm(updated)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
