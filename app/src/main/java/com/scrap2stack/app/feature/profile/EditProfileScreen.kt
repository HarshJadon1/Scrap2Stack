package com.scrap2stack.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.BrutalHeaderTag
import com.scrap2stack.app.core.ui.components.BrutalTopBar
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.brutalTextFieldColors
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.ExperienceLevel
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val fieldColors = brutalTextFieldColors()
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
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "PROFILE // EDIT",
                title = "Edit Profile",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
                .imePadding()
        ) {
            if (uiState is ProfileUiState.Loading) {
                LoadingView()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )

                    BrutalHeaderTag(text = "EXPERIENCE LEVEL")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExperienceLevel.entries.forEach { level ->
                            val isSelected = experienceLevel == level
                            Box(
                                modifier = Modifier
                                    .tactileBrutalCard(
                                        cornerRadius = 10.dp,
                                        containerColor = if (isSelected) ElectricMint else brutal.surface,
                                        borderColor = brutal.border,
                                        borderWidth = 2.dp,
                                        shadowColor = brutal.shadow,
                                        shadowOffsetX = 2.dp,
                                        shadowOffsetY = 2.dp,
                                        onClick = { experienceLevel = level }
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    level.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = if (isSelected) BrutalInkBlack else brutal.textPrimary
                                )
                            }
                        }
                    }

                    // Skills Entry
                    BrutalHeaderTag(text = "SKILLS")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = skillInput,
                            onValueChange = { skillInput = it },
                            label = { Text("Add Skill") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .tactileBrutalCard(
                                    cornerRadius = 12.dp,
                                    containerColor = ElectricMint,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = BrutalShadowBlack,
                                    shadowOffsetX = 3.dp,
                                    shadowOffsetY = 3.dp,
                                    onClick = {
                                        if (skillInput.isNotBlank() && !skills.contains(skillInput.trim())) {
                                            skills.add(skillInput.trim())
                                            skillInput = ""
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Skill", tint = BrutalInkBlack)
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skills.forEach { skill ->
                            Row(
                                modifier = Modifier
                                    .tactileBrutalCard(
                                        cornerRadius = 10.dp,
                                        containerColor = brutal.surfaceRaised,
                                        borderColor = brutal.border,
                                        borderWidth = 1.5.dp,
                                        shadowColor = brutal.shadow,
                                        shadowOffsetX = 2.dp,
                                        shadowOffsetY = 2.dp,
                                        onClick = { skills.remove(skill) }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    skill,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = brutal.textPrimary
                                )
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = brutal.textMuted)
                            }
                        }
                    }

                    // Interests Entry
                    BrutalHeaderTag(text = "INTERESTS")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = interestInput,
                            onValueChange = { interestInput = it },
                            label = { Text("Add Interest") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .tactileBrutalCard(
                                    cornerRadius = 12.dp,
                                    containerColor = ElectricMint,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = BrutalShadowBlack,
                                    shadowOffsetX = 3.dp,
                                    shadowOffsetY = 3.dp,
                                    onClick = {
                                        if (interestInput.isNotBlank() && !interests.contains(interestInput.trim())) {
                                            interests.add(interestInput.trim())
                                            interestInput = ""
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Interest", tint = BrutalInkBlack)
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interests.forEach { interest ->
                            Row(
                                modifier = Modifier
                                    .tactileBrutalCard(
                                        cornerRadius = 10.dp,
                                        containerColor = brutal.surfaceRaised,
                                        borderColor = brutal.border,
                                        borderWidth = 1.5.dp,
                                        shadowColor = brutal.shadow,
                                        shadowOffsetX = 2.dp,
                                        shadowOffsetY = 2.dp,
                                        onClick = { interests.remove(interest) }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    interest,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = brutal.textPrimary
                                )
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = brutal.textMuted)
                            }
                        }
                    }

                    BrutalHeaderTag(text = "SOCIAL & LINKS")
                    OutlinedTextField(
                        value = githubUrl,
                        onValueChange = { githubUrl = it },
                        label = { Text("GitHub URL") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://github.com/username") },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = linkedinUrl,
                        onValueChange = { linkedinUrl = it },
                        label = { Text("LinkedIn URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = portfolioUrl,
                        onValueChange = { portfolioUrl = it },
                        label = { Text("Portfolio URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )

                    if (editUiState is EditProfileUiState.Error) {
                        Text(
                            text = (editUiState as EditProfileUiState.Error).message,
                            color = StatusAbandoned,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Scrap2StackButton(
                        text = "SAVE PROFILE",
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
                    LoadingView(
                        modifier = Modifier
                            .matchParentSize()
                            .background(brutal.canvas.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}
