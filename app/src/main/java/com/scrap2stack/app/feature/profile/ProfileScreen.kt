package com.scrap2stack.app.feature.profile

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackOutlinedButton
import com.scrap2stack.app.core.ui.components.SkillChip
import com.scrap2stack.app.core.ui.components.bouncingClickable
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.RevivalEmerald

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    LoadingView(modifier = Modifier.padding(20.dp))
                }
                is ProfileUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProfile() }) {
                            Text("Retry")
                        }
                    }
                }
                is ProfileUiState.Success -> {
                    ProfileContent(
                        developer = state.developer,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToEditProfile = onNavigateToEditProfile
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    developer: Developer,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(18.dp))
        
        // Hero Profile Glass Card
        val cardBorder = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                ElectricMint.copy(alpha = 0.2f),
                Color.Transparent
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF131D33), Color(0xFF0F172A))
                    )
                )
                .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(26.dp))
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(
                        name = developer.name.ifBlank { "User" },
                        modifier = Modifier.size(76.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = developer.name.ifBlank { "Developer" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = (-0.3).sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (developer.username.isNotBlank()) "@${developer.username}" else "@developer",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = ElectricMint.copy(alpha = 0.85f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .bouncingClickable(scaleDown = 0.92f, onClick = onNavigateToSettings)
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (developer.bio.isNotBlank()) developer.bio else "Passionate developer revamping open source side projects.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Scrap2StackOutlinedButton(
                    text = "Edit Profile",
                    onClick = onNavigateToEditProfile
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats Row (Charms & Experience)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(
                label = "Total Charms",
                value = developer.charms.toString(),
                accentColor = ElectricMint,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "Experience Level",
                value = developer.experienceLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                accentColor = PrimaryIndigoLight,
                modifier = Modifier.weight(1f)
            )
        }

        // Skills Section
        if (developer.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "Core Skills",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.skills.forEach { skill ->
                    SkillChip(skill = skill)
                }
            }
        }

        // Interests Section
        if (developer.interests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Interests",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.interests.forEach { interest ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = interest,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Social Links Section
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Social & Links",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            ),
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(10.dp))

        if (developer.githubUrl.isNotBlank()) {
            SocialLinkItem(icon = Icons.Default.Code, label = "GitHub", value = developer.githubUrl)
        }
        if (developer.linkedinUrl.isNotBlank()) {
            SocialLinkItem(icon = Icons.Default.Link, label = "LinkedIn", value = developer.linkedinUrl)
        }
        if (developer.portfolioUrl.isNotBlank()) {
            SocialLinkItem(icon = Icons.Default.Public, label = "Portfolio", value = developer.portfolioUrl)
        }
        
        if (developer.githubUrl.isBlank() && developer.linkedinUrl.isBlank() && developer.portfolioUrl.isBlank()) {
            Text(
                text = "No links added yet. Tap 'Edit Profile' to add links.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun ProfileStatCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val statBorder = Brush.linearGradient(
        listOf(accentColor.copy(alpha = 0.35f), Color.Transparent)
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF10192A))
            .border(BorderStroke(1.dp, statBorder), RoundedCornerShape(20.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SocialLinkItem(icon: ImageVector, label: String, value: String) {
    val uriHandler = LocalUriHandler.current

    val openUrl = {
        if (value.isNotBlank()) {
            val formattedUrl = if (!value.startsWith("http://") && !value.startsWith("https://")) {
                "https://$value"
            } else {
                value
            }
            try {
                uriHandler.openUri(formattedUrl)
            } catch (e: Exception) {
                Log.e("SocialLinkItem", "Failed to open link: $formattedUrl", e)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF10192A))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)), RoundedCornerShape(16.dp))
            .bouncingClickable(scaleDown = 0.98f, onClick = openUrl)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ElectricMint
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = Color.White
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open Link",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
