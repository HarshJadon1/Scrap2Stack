package com.scrap2stack.app.feature.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectAnalysis
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.domain.service.ScrapAIEngine
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.StatusCompleted
import com.scrap2stack.app.ui.theme.StatusReviving
import com.scrap2stack.app.ui.theme.brutalColors

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
    val brutal = MaterialTheme.brutalColors
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
            containerColor = brutal.surface,
            titleContentColor = brutal.textPrimary,
            textContentColor = brutal.textMuted,
            title = { Text("Delete Project?", fontWeight = FontWeight.Black) },
            text = { Text("Are you sure you want to delete '${successState.project.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteProject(projectId)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusAbandoned)
                ) {
                    Text("Delete", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = brutal.textPrimary)
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
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "PROJECT // DOSSIER",
                title = "Project Details",
                onNavigateBack = onNavigateBack,
                actions = {
                    if (successState != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .tactileBrutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = if (isSaved) ElectricMint else brutal.surfaceRaised,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = brutal.shadow,
                                    shadowOffsetX = 3.dp,
                                    shadowOffsetY = 3.dp,
                                    onClick = { viewModel.toggleSave(projectId) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isSaved) "Unsave Project" else "Save Project",
                                tint = if (isSaved) BrutalInkBlack else brutal.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isOwner) {
                            Box {
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
                                            onClick = { showMenu = true }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = brutal.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    containerColor = brutal.surface
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Project", color = brutal.textPrimary, fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = brutal.accentOnSurface) },
                                        onClick = {
                                            showMenu = false
                                            showEditDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Project", color = StatusAbandoned, fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusAbandoned) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
        ) {
            when (val state = uiState) {
                is ProjectDetailsState.Loading -> {
                    LoadingView()
                }
                is ProjectDetailsState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadProjectDetails(projectId) }
                    )
                }
                is ProjectDetailsState.Success -> {
                    ProjectDetailsContent(
                        project = state.project,
                        analysis = (analysisState as? ProjectAnalysisState.Success)?.analysis,
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectDetailsContent(
    project: Project,
    analysis: ProjectAnalysis?,
    onNavigateToScrapAI: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToWorkspace: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        // Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 16.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = brutal.shadow,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val statusColor = when (project.status.name) {
                        "ABANDONED" -> StatusAbandoned
                        "REVIVING" -> StatusReviving
                        "COMPLETED" -> StatusCompleted
                        else -> Color.Gray
                    }
                    StatusChip(status = project.status.name, color = statusColor)
                }

                if (project.technologies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = project.technologies.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = brutal.accentOnSurface
                    )
                }
            }
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

        Spacer(modifier = Modifier.height(20.dp))

        // AI Insights Section
        if (analysis != null && analysis.recommendations.isNotEmpty()) {
            AIInsightsSection(analysis)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Overview
        SectionHeader(title = "Overview")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 14.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = brutal.shadow,
                    shadowOffsetX = 3.dp,
                    shadowOffsetY = 3.dp
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = brutal.textPrimary
                )

                if (project.problem.isNotBlank()) {
                    HorizontalDivider(color = brutal.border.copy(alpha = 0.25f))
                    BrutalHeaderTag(text = "WHY WAS IT ABANDONED?")
                    Text(
                        text = project.problem,
                        style = MaterialTheme.typography.bodyMedium,
                        color = brutal.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Analysis Teaser
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .tactileBrutalCard(
                    cornerRadius = 16.dp,
                    containerColor = brutal.surfaceRaised,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = PrimaryIndigo,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp,
                    onClick = onNavigateToScrapAI
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .brutalCard(
                            cornerRadius = 10.dp,
                            containerColor = ElectricMint,
                            borderColor = brutal.border,
                            borderWidth = 1.5.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrutalInkBlack)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "ScrapAI Deep Audit",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )
                    Text(
                        "View technical risks, required skills, and roadmap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Required Skills
        if (project.requiredSkills.isNotEmpty()) {
            SectionHeader(title = "Required Skills")
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                project.requiredSkills.forEach { skill ->
                    SkillChip(skill = skill)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Workflow CTA: Open Workspace
        Scrap2StackButton(
            text = "🚀 ENTER PROJECT WORKSPACE",
            onClick = onNavigateToWorkspace
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .tactileBrutalCard(
                        cornerRadius = 12.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp,
                        onClick = onNavigateToMatches
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = brutal.accentOnSurface)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Find Matches", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = brutal.textPrimary)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .tactileBrutalCard(
                        cornerRadius = 12.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp,
                        onClick = onNavigateToScrapAI
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryIndigo)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Roadmap", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), color = brutal.textPrimary)
                }
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
                        } catch (_: Exception) {
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = brutal.accentOnSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View on GitHub", fontWeight = FontWeight.Bold, color = brutal.accentOnSurface)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AIInsightsSection(analysis: ProjectAnalysis) {
    val brutal = MaterialTheme.brutalColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 14.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = ElectricMint,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Psychology,
                contentDescription = null,
                tint = brutal.accentOnSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "SCRAPAI RECOMMENDS...",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                ),
                color = brutal.accentOnSurface
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        analysis.recommendations.take(3).forEach { step ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
                    tint = brutal.accentOnSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = step, style = MaterialTheme.typography.bodySmall, color = brutal.textPrimary)
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
    val brutal = MaterialTheme.brutalColors
    val fieldColors = brutalTextFieldColors()
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
        containerColor = brutal.surface,
        titleContentColor = brutal.textPrimary,
        title = { Text("Edit Project", fontWeight = FontWeight.Black) },
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
                    singleLine = true,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = problem,
                    onValueChange = { problem = it },
                    label = { Text("Why was it abandoned?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = technologies,
                    onValueChange = { technologies = it },
                    label = { Text("Technologies (Comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Required Skills (Comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                BrutalHeaderTag(text = "CATEGORY")
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
                        val selected = category == cat
                        Box(
                            modifier = Modifier
                                .tactileBrutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = if (selected) ElectricMint else brutal.surfaceRaised,
                                    borderColor = brutal.border,
                                    borderWidth = 1.5.dp,
                                    shadowColor = brutal.shadow,
                                    shadowOffsetX = 2.dp,
                                    shadowOffsetY = 2.dp,
                                    onClick = { category = cat }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                cat,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selected) BrutalInkBlack else brutal.textPrimary
                            )
                        }
                    }
                }

                BrutalHeaderTag(text = "PROJECT STATUS")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProjectStatus.entries.forEach { st ->
                        val selected = status == st
                        Box(
                            modifier = Modifier
                                .tactileBrutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = if (selected) ElectricMint else brutal.surfaceRaised,
                                    borderColor = brutal.border,
                                    borderWidth = 1.5.dp,
                                    shadowColor = brutal.shadow,
                                    shadowOffsetX = 2.dp,
                                    shadowOffsetY = 2.dp,
                                    onClick = { status = st }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                st.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (selected) BrutalInkBlack else brutal.textPrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = githubUrl,
                    onValueChange = { githubUrl = it },
                    label = { Text("GitHub URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            TextButton(
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
                Text("Save Changes", fontWeight = FontWeight.Black, color = brutal.accentOnSurface)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = brutal.textMuted)
            }
        }
    )
}
