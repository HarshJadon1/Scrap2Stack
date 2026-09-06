package com.scrap2stack.app.feature.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.Scrap2StackCard
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.core.ui.components.StatusChip
import com.scrap2stack.app.data.remote.dto.TaskDto

@Composable
fun TasksScreen(
    projectId: String,
    tasks: List<TaskDto>,
    onStatusUpdate: (String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf("TODO") }
    val statuses = listOf("TODO", "IN_PROGRESS", "IN_REVIEW", "BLOCKED", "COMPLETED")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        SectionHeader(title = "Project Tasks")

        ScrollableTabRow(
            selectedTabIndex = statuses.indexOf(selectedStatus),
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            val filteredTasks = tasks.filter { it.status == selectedStatus }
            
            if (filteredTasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tasks in this category", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    TaskCard(task, onStatusUpdate)
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskDto,
    onStatusUpdate: (String, String) -> Unit
) {
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
                
                val priorityColor = when (task.priority) {
                    "LOW" -> Color.Gray
                    "MEDIUM" -> MaterialTheme.colorScheme.primary
                    "HIGH" -> MaterialTheme.colorScheme.secondary
                    "CRITICAL" -> MaterialTheme.colorScheme.error
                    else -> Color.Gray
                }
                StatusChip(status = task.priority, color = priorityColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Assignee", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(task.assigneeId ?: "Unassigned", style = MaterialTheme.typography.bodySmall)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Due Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(task.dueDate ?: "No date", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "#${task.skill}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Status Update Buttons
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (task.status == "TODO") {
                    Button(onClick = { onStatusUpdate(task.id, "IN_PROGRESS") }, modifier = Modifier.weight(1f)) {
                        Text("Start", style = MaterialTheme.typography.labelSmall)
                    }
                } else if (task.status == "IN_PROGRESS") {
                    Button(onClick = { onStatusUpdate(task.id, "IN_REVIEW") }, modifier = Modifier.weight(1f)) {
                        Text("Finish", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
