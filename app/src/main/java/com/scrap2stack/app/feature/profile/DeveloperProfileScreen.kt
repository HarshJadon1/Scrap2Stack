package com.scrap2stack.app.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.Scrap2StackOutlinedButton
import com.scrap2stack.app.core.ui.components.SkillChip
import com.scrap2stack.app.data.local.MockData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeveloperProfileScreen(
    developerId: String,
    onNavigateBack: () -> Unit,
    onRequestCollaboration: () -> Unit
) {
    val developer = MockData.developers.find { it.id == developerId } ?: MockData.developers.first()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "@${developer.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Bio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                text = developer.bio,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStatItem(label = "Charms", value = developer.charms.toString())
                ProfileStatItem(label = "Experience", value = developer.experienceLevel)
                ProfileStatItem(label = "GitHub", value = developer.githubUsername)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Skills", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            FlowRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.skills.forEach { skill ->
                    SkillChip(skill = skill)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Interests", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            FlowRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.interests.forEach { interest ->
                    SuggestionChip(onClick = {}, label = { Text(interest) })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Scrap2StackButton(
                text = "Request to Collaborate",
                onClick = onRequestCollaboration
            )
            
            Scrap2StackOutlinedButton(
                text = "View GitHub Profile",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
