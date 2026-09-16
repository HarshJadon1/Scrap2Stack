package com.scrap2stack.app.feature.profile

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.domain.model.ExperienceLevel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val editUiState by viewModel.editUiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var githubUrl by remember { mutableStateOf("") }
    var linkedinUrl by remember { mutableStateOf("") }
    var portfolioUrl by remember { mutableStateOf("") }
    var experienceLevel by remember { mutableStateOf(ExperienceLevel.BEGINNER) }
    
    val skills = remember { mutableStateListOf<String>() }
    val interests = remember { mutableStateListOf<String>() }
    
    var skillInput by remember { mutableStateOf("") }
    var interestInput by remember { mutableStateOf("") }

    // Initialize fields when profile is loaded
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val dev = (uiState as ProfileUiState.Success).developer
            name = dev.name
            username = dev.username
            bio = dev.bio
            githubUrl = dev.githubUrl
            linkedinUrl = dev.linkedinUrl
            portfolioUrl = dev.portfolioUrl
            experienceLevel = dev.experienceLevel
            skills.clear()
            skills.addAll(dev.skills)
            interests.clear()
            interests.addAll(dev.interests)
        }
    }

    // Handle success update
    LaunchedEffect(editUiState) {
        if (editUiState is EditProfileUiState.Success) {
            onNavigateBack()
            viewModel.resetEditState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState is ProfileUiState.Loading) {
                LoadingView()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text("Experience Level", style = MaterialTheme.typography.titleSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExperienceLevel.entries.forEach { level ->
                            FilterChip(
                                selected = experienceLevel == level,
                                onClick = { experienceLevel = level },
                                label = { Text(level.name) }
                            )
                        }
                    }

                    // Skills Entry
                    Text("Skills", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = skillInput,
                            onValueChange = { skillInput = it },
                            label = { Text("Add Skill") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            if (skillInput.isNotBlank() && !skills.contains(skillInput.trim())) {
                                skills.add(skillInput.trim())
                                skillInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Skill")
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        skills.forEach { skill ->
                            InputChip(
                                selected = true,
                                onClick = { skills.remove(skill) },
                                label = { Text(skill) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }

                    // Interests Entry
                    Text("Interests", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = interestInput,
                            onValueChange = { interestInput = it },
                            label = { Text("Add Interest") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            if (interestInput.isNotBlank() && !interests.contains(interestInput.trim())) {
                                interests.add(interestInput.trim())
                                interestInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Interest")
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        interests.forEach { interest ->
                            InputChip(
                                selected = true,
                                onClick = { interests.remove(interest) },
                                label = { Text(interest) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }

                    Text("Links", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = githubUrl,
                        onValueChange = { githubUrl = it },
                        label = { Text("GitHub URL") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://github.com/username") }
                    )
                    OutlinedTextField(
                        value = linkedinUrl,
                        onValueChange = { linkedinUrl = it },
                        label = { Text("LinkedIn URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = portfolioUrl,
                        onValueChange = { portfolioUrl = it },
                        label = { Text("Portfolio URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (editUiState is EditProfileUiState.Error) {
                        Text(
                            text = (editUiState as EditProfileUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Scrap2StackButton(
                        text = "Save Profile",
                        onClick = {
                            viewModel.updateProfile(
                                name = name,
                                username = username,
                                bio = bio,
                                skills = skills.toList(),
                                interests = interests.toList(),
                                experienceLevel = experienceLevel,
                                githubUrl = githubUrl,
                                linkedinUrl = linkedinUrl,
                                portfolioUrl = portfolioUrl
                            )
                        },
                        enabled = editUiState !is EditProfileUiState.Loading && name.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (editUiState is EditProfileUiState.Loading) {
                    LoadingView(modifier = Modifier.matchParentSize())
                }
            }
        }
    }
}
