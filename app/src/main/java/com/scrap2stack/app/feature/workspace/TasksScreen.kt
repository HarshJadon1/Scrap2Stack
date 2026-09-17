package com.scrap2stack.app.feature.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.network.GeminiAiService
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.Scrap2StackCard
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.core.ui.components.StatusChip
import com.scrap2stack.app.domain.model.Task
import com.scrap2stack.app.domain.model.TaskPriority
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    projectId: String,
    tasks: List<Task>,
    onCreateTask: (String, String, TaskPriority) -> Unit,
    onStatusUpdate: (String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf("TODO") }
    val statuses = listOf("TODO", "IN_PROGRESS", "REVIEW", "COMPLETED")
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "Project Tasks")
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                }
            }

            ScrollableTabRow(
                selectedTabIndex = statuses.indexOf(selectedStatus).coerceAtLeast(0),
                containerColor = Color.Transparent,
                divider = {},
                edgePadding = 0.dp
            ) {
                statuses.forEach { status ->
                    Tab(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        text = { Text(status.replace("_", " ")) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                val filteredTasks = tasks.filter { it.status.name == selectedStatus }
                
                if (filteredTasks.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(0.6f), contentAlignment = Alignment.Center) {
                            Text("No tasks in this category", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    items(filteredTasks) { task ->
                        TaskCard(task, projectId, onStatusUpdate)
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateTaskDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, skill, priority ->
                    onCreateTask(title, skill, priority)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    projectId: String,
    onStatusUpdate: (String, String) -> Unit
) {
    var showAiSolution by remember { mutableStateOf(false) }
    var aiSolutionText by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiAiService() }

    Scrap2StackCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                val priorityColor = when (task.priority.name) {
                    "LOW" -> Color.Gray
                    "MEDIUM" -> MaterialTheme.colorScheme.primary
                    "HIGH" -> MaterialTheme.colorScheme.secondary
                    "URGENT" -> MaterialTheme.colorScheme.error
                    else -> Color.Gray
                }
                StatusChip(status = task.priority.name, color = priorityColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Assignee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(task.assignee ?: "Unassigned", style = MaterialTheme.typography.bodySmall)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Due Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(task.dueDate ?: "No date", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            if (task.skill.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "#${task.skill}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ScrapAI Solution Button
            OutlinedButton(
                onClick = {
                    showAiSolution = !showAiSolution
                    if (showAiSolution && aiSolutionText == null) {
                        isLoadingAi = true
                        coroutineScope.launch {
                            val res = geminiService.generateTaskSolution(
                                taskTitle = task.title,
                                skill = task.skill.ifBlank { "Software Architecture" },
                                projectName = projectId
                            )
                            aiSolutionText = res.getOrDefault("Failed to generate AI solution.")
                            isLoadingAi = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showAiSolution) "Hide AI Solution" else "ScrapAI Code Suggestion")
            }

            AnimatedVisibility(visible = showAiSolution) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    if (isLoadingAi) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini 1.5 Flash is generating code suggestion...", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text(
                            text = aiSolutionText ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Status Update Buttons
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (task.status.name) {
                    "TODO" -> {
                        Button(onClick = { onStatusUpdate(task.id, "IN_PROGRESS") }, modifier = Modifier.weight(1f)) {
                            Text("Start Task", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    "IN_PROGRESS" -> {
                        Button(onClick = { onStatusUpdate(task.id, "REVIEW") }, modifier = Modifier.weight(1f)) {
                            Text("Submit for Review", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    "REVIEW" -> {
                        Button(onClick = { onStatusUpdate(task.id, "COMPLETED") }, modifier = Modifier.weight(1f)) {
                            Text("Approve & Complete (+10 Charms)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, TaskPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Workspace Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = skill,
                    onValueChange = { skill = it },
                    label = { Text("Required Skill (e.g. Jetpack Compose)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Priority Level", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = selectedPriority == p,
                            onClick = { selectedPriority = p },
                            label = { Text(p.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Scrap2StackButton(
                text = "Create Task",
                onClick = { onCreate(title, skill, selectedPriority) },
                enabled = title.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
