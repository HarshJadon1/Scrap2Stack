package com.scrap2stack.app.feature.profile

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeveloperProfileScreen(
    developerId: String,
    viewModel: DeveloperProfileViewModel,
    onNavigateBack: () -> Unit,
    onRequestCollaboration: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(developerId) {
        viewModel.loadDeveloperProfile(developerId)
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            BrutalTopBar(
                tag = "DEV // DOSSIER",
                title = "Developer Profile",
                onNavigateBack = onNavigateBack
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
                is DeveloperProfileUiState.Loading -> LoadingView()
                is DeveloperProfileUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadDeveloperProfile(developerId) }
                )
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
    val brutal = MaterialTheme.brutalColors
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
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

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
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(name = developer.name, modifier = Modifier.size(88.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = developer.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = brutal.textPrimary
                )
                Text(
                    text = if (developer.username.isNotBlank()) "@${developer.username}" else "No username",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = brutal.accentOnSurface
                )
            }
        }

        if (developer.bio.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
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
                Column {
                    BrutalHeaderTag(text = "BIO")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = developer.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = brutal.textPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScoreCard(
                label = "Total Charms",
                score = developer.charms.toString(),
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                label = "Experience",
                score = developer.experienceLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                modifier = Modifier.weight(1f)
            )
        }

        if (developer.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(title = "Skills")
            Row(
                modifier = Modifier.padding(vertical = 4.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.skills.forEach { skill ->
                    SkillChip(skill = skill)
                }
            }
        }

        if (developer.interests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Interests")
            Row(
                modifier = Modifier.padding(vertical = 4.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.interests.forEach { interest ->
                    BrutalBadge(
                        text = interest,
                        containerColor = brutal.surfaceRaised,
                        contentColor = brutal.textPrimary,
                        borderColor = brutal.border
                    )
                }
            }
        }

        if (developer.githubUrl.isNotBlank() || developer.linkedinUrl.isNotBlank() || developer.portfolioUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(title = "Social & Portfolio")

            Spacer(modifier = Modifier.height(8.dp))

            if (developer.githubUrl.isNotBlank()) {
                SocialLinkItem(icon = Icons.Default.Code, label = "GitHub", value = developer.githubUrl)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (developer.linkedinUrl.isNotBlank()) {
                SocialLinkItem(icon = Icons.Default.Link, label = "LinkedIn", value = developer.linkedinUrl)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (developer.portfolioUrl.isNotBlank()) {
                SocialLinkItem(icon = Icons.Default.Public, label = "Portfolio", value = developer.portfolioUrl)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Scrap2StackButton(
            text = "INVITE TO COLLABORATE",
            onClick = onRequestCollaboration
        )

        if (developer.githubUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Scrap2StackOutlinedButton(
                text = "VIEW GITHUB PROFILE",
                onClick = { openUrl(developer.githubUrl) },
                shadowColor = PrimaryIndigo
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
