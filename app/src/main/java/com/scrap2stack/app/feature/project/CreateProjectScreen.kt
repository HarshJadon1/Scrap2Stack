package com.scrap2stack.app.feature.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.domain.model.ProjectStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel,
    onNavigateBack: () -> Unit,
    onProjectCreated: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Web") }
    var technologies by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ProjectStatus.ABANDONED) }
    var githubUrl by remember { mutableStateOf("") }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Project") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Give your idea a second life",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Short Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = problem,
                onValueChange = { problem = it },
                label = { Text("The Problem (Why it was abandoned?) *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = technologies,
                onValueChange = { technologies = it },
                label = { Text("Technologies (Comma separated) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Kotlin, Python, React") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                label = { Text("Required Skills (Comma separated) *") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Machine Learning, UI Design") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Web", "Mobile", "AI/ML", "DevOps").forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = githubUrl,
                onValueChange = { githubUrl = it },
                label = { Text("GitHub Repository URL (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://github.com/user/project") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is CreateProjectUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Scrap2StackButton(
                    text = "Create Project",
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
