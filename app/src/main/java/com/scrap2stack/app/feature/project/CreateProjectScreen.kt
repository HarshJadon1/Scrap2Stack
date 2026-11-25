package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.network.GitHubService
import com.scrap2stack.app.core.ui.components.BrutalHeaderTag
import com.scrap2stack.app.core.ui.components.BrutalTopBar
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.brutalTextFieldColors
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel,
    onNavigateBack: () -> Unit,
    onProjectCreated: (String) -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val fieldColors = brutalTextFieldColors()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Web") }
    var technologies by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ProjectStatus.ABANDONED) }
    var githubUrl by remember { mutableStateOf("") }
    var isFetchingRepo by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val gitHubService = remember { GitHubService() }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreateProjectUiState.Success -> {
                if (state.project.id.isNotBlank()) {
                    onProjectCreated(state.project.id)
                }
            }
            is CreateProjectUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = brutal.canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BrutalTopBar(
                tag = "NEW // STACK",
                title = "Create Project",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Give your idea a second life",
                style = MaterialTheme.typography.bodyMedium,
                color = brutal.textMuted,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .brutalCard(
                        cornerRadius = 16.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = PrimaryIndigo,
                        shadowOffsetX = 4.dp,
                        shadowOffsetY = 4.dp
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = brutal.accentOnSurface
                        )
                        Text(
                            text = "Auto-Fill with GitHub & AI",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = brutal.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste your GitHub repository to auto-fill title, description & tech stack.",
                        style = MaterialTheme.typography.bodySmall,
                        color = brutal.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = githubUrl,
                            onValueChange = { githubUrl = it },
                            placeholder = { Text("https://github.com/owner/repo", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        val canFetch = !isFetchingRepo && githubUrl.isNotBlank()
                        Box(
                            modifier = Modifier
                                .height(52.dp)
                                .tactileBrutalCard(
                                    cornerRadius = 12.dp,
                                    containerColor = if (canFetch) ElectricMint else brutal.surfaceRaised,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = BrutalShadowBlack,
                                    shadowOffsetX = 3.dp,
                                    shadowOffsetY = 3.dp,
                                    enabled = canFetch,
                                    onClick = {
                                        val parsed = gitHubService.parseRepoUrl(githubUrl)
                                        if (parsed == null) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Please enter a valid GitHub repository URL")
                                            }
                                        } else {
                                            isFetchingRepo = true
                                            coroutineScope.launch {
                                                val (owner, repo) = parsed
                                                val result = gitHubService.fetchRepoInfo(owner, repo)
                                                result.onSuccess { live ->
                                                    val info = live.data
                                                    if (name.isBlank() || name == "New Project") {
                                                        name = info.name.replace("-", " ").replace("_", " ").split(" ")
                                                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                                    }
                                                    if (description.isBlank() && !info.description.isNullOrBlank()) {
                                                        description = info.description
                                                    }
                                                    if (problem.isBlank()) {
                                                        problem = "Project was abandoned and needs revival, modern refactoring, and active contributors."
                                                    }
                                                    val primaryLang = info.primaryLanguage?.takeIf { it.isNotBlank() } ?: "Kotlin"
                                                    if (technologies.isBlank()) {
                                                        technologies = "$primaryLang, GitHub"
                                                    }
                                                    if (skills.isBlank()) {
                                                        skills = "$primaryLang Developer, Maintainer"
                                                    }
                                                    val lang = primaryLang.lowercase()
                                                    category = when {
                                                        lang.contains("kotlin") || lang.contains("swift") || lang.contains("dart") || lang.contains("flutter") -> "Mobile"
                                                        lang.contains("python") || lang.contains("ai") || lang.contains("jupyter") -> "AI/ML"
                                                        lang.contains("go") || lang.contains("docker") || lang.contains("shell") -> "DevOps"
                                                        else -> "Web"
                                                    }
                                                    val msg = if (live.isLiveData) "⚡ Repository details auto-filled!" else "⚠ ${live.statusNote ?: "Auto-filled cached template"}"
                                                    snackbarHostState.showSnackbar(msg)
                                                }.onFailure {
                                                    snackbarHostState.showSnackbar("Could not fetch repo: ${it.localizedMessage}")
                                                }
                                                isFetchingRepo = false
                                            }
                                        }
                                    }
                                )
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFetchingRepo) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = BrutalInkBlack
                                )
                            } else {
                                Text(
                                    "AUTO",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = if (canFetch) BrutalInkBlack else brutal.textMuted
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Short Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = problem,
                onValueChange = { problem = it },
                label = { Text("The Problem (Why it was abandoned?) *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = technologies,
                onValueChange = { technologies = it },
                label = { Text("Technologies (Comma separated) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Kotlin, Python, React") },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Required Skills (Comma separated) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Machine Learning, UI Design") },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            BrutalHeaderTag(
                text = "CATEGORY",
                modifier = Modifier.align(Alignment.Start)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Web", "Mobile", "AI/ML", "DevOps").forEach { cat ->
                    val isSelected = category == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = if (isSelected) ElectricMint else brutal.surface,
                                borderColor = brutal.border,
                                borderWidth = 2.dp,
                                shadowColor = brutal.shadow,
                                shadowOffsetX = 2.dp,
                                shadowOffsetY = 2.dp,
                                onClick = { category = cat }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            cat,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = if (isSelected) BrutalInkBlack else brutal.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = githubUrl,
                onValueChange = { githubUrl = it },
                label = { Text("GitHub Repository URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://github.com/user/project") },
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (uiState is CreateProjectUiState.Loading) {
                CircularProgressIndicator(color = brutal.accentOnSurface)
            } else {
                Scrap2StackButton(
                    text = "CREATE PROJECT",
                    onClick = {
                        val techList = technologies.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val skillList = skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        viewModel.createProject(
                            name = name,
                            problem = problem,
                            description = description,
                            category = category,
                            teamSize = 4,
                            technologies = techList,
                            status = status,
                            requiredSkills = skillList,
                            githubUrl = githubUrl
                        )
                    },
                    enabled = name.isNotBlank() && description.isNotBlank() && problem.isNotBlank() && technologies.isNotBlank() && skills.isNotBlank()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
