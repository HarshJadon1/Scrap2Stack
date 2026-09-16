package com.scrap2stack.app.feature.profile

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Developer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeveloperProfileScreen(
    developerId: String,
    viewModel: DeveloperProfileViewModel,
    onNavigateBack: () -> Unit,
    onRequestCollaboration: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(developerId) {
        viewModel.loadDeveloperProfile(developerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is DeveloperProfileUiState.Loading -> LoadingView()
                is DeveloperProfileUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadDeveloperProfile(developerId) })
                is DeveloperProfileUiState.Success -> {
                    DeveloperProfileContent(
                        developer = state.developer,
                        onRequestCollaboration = onRequestCollaboration
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeveloperProfileContent(
    developer: Developer,
    onRequestCollaboration: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val openUrl = { url: String ->
        if (url.isNotBlank()) {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            try {
                uriHandler.openUri(formattedUrl)
            } catch (e: Exception) {
                Log.e("DeveloperProfileContent", "Failed to open URL: $formattedUrl", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(name = developer.name, modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = developer.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (developer.username.isNotBlank()) "@${developer.username}" else "No username",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (developer.bio.isNotBlank()) {
            Text("Bio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = developer.bio,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProfileStatItem(label = "Charms", value = developer.charms.toString())
            ProfileStatItem(label = "Experience", value = developer.experienceLevel.name.lowercase().replaceFirstChar { it.uppercase() })
        }

        if (developer.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.skills.forEach { skill ->
                    SkillChip(skill = skill)
                }
            }
        }

        if (developer.interests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.padding(vertical = 8.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.interests.forEach { interest ->
                    SuggestionChip(onClick = {}, label = { Text(interest) })
                }
            }
        }

        if (developer.githubUrl.isNotBlank() || developer.linkedinUrl.isNotBlank() || developer.portfolioUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Social & Portfolio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))

            if (developer.githubUrl.isNotBlank()) {
                SocialLinkItem(icon = Icons.Default.Code, label = "GitHub", value = developer.githubUrl)
            }
            if (developer.linkedinUrl.isNotBlank()) {
                SocialLinkItem(icon = Icons.Default.Link, label = "LinkedIn", value = developer.linkedinUrl)
            }
            if (developer.portfolioUrl.isNotBlank()) {
                SocialLinkItem(icon = Icons.Default.Public, label = "Portfolio", value = developer.portfolioUrl)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Scrap2StackButton(
            text = "Invite to Collaborate",
            onClick = onRequestCollaboration
        )
        
        if (developer.githubUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Scrap2StackOutlinedButton(
                text = "View GitHub Profile",
                onClick = { openUrl(developer.githubUrl) }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
