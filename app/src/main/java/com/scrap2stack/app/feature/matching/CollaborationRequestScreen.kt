package com.scrap2stack.app.feature.matching

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.data.local.MockData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationRequestScreen(
    projectId: String,
    developerId: String,
    onNavigateBack: () -> Unit,
    onRequestSent: () -> Unit
) {
    val developer = MockData.developers.find { it.id == developerId } ?: MockData.developers.first()
    val project = MockData.projects.find { it.id == projectId } ?: MockData.projects.first()
    
    var message by remember { mutableStateOf("I think your experience would be a great fit for this project.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Developer") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(name = developer.name, modifier = Modifier.size(80.dp))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Invite ${developer.name} to this project?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Project: ${project.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Optional message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(modifier = Modifier.weight(1f))

            Scrap2StackButton(
                text = "Send Invitation",
                onClick = onRequestSent
            )
            
            TextButton(onClick = onNavigateBack) {
                Text("Cancel")
            }
        }
    }
}
