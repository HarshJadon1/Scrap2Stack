package com.scrap2stack.app.feature.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.scrap2stack.app.core.network.GeminiAiService
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Task
import com.scrap2stack.app.domain.model.TaskPriority
import com.scrap2stack.app.feature.chat.components.CodeBlockBubble
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoDark
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.RevivalEmerald
import com.scrap2stack.app.ui.theme.StatusAbandoned
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    projectId: String,
    tasks: List<Task>,
    onCreateTask: (String, String, TaskPriority) -> Unit,
    onStatusUpdate: (String, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf("TODO") }
    val statuses = listOf(
        "TODO" to "To Do",
        "IN_PROGRESS" to "In Progress",
        "REVIEW" to "In Review",
        "COMPLETED" to "Completed"
    )
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Row with Add Task Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Interactive Kanban",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        ),
                        color = ElectricMint
                    )
                    Text(
                        text = "Workspace Tasks",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(PrimaryIndigo, PrimaryIndigoDark))
                        )
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            RoundedCornerShape(14.dp)
                        )
                        .bouncingClickable(scaleDown = 0.92f) { showCreateDialog = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Task",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Task",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Kanban Column Lanes Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { (statusKey, statusLabel) ->
                    val isSelected = selectedStatus == statusKey
                    val count = tasks.count { it.status.name == statusKey }

                    val pillBg by animateColorAsState(
                        targetValue = if (isSelected) ElectricMint.copy(alpha = 0.15f) else Color(0xFF10192A),
                        label = "pill_bg"
                    )
                    val pillBorder by animateColorAsState(
                        targetValue = if (isSelected) ElectricMint.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f),
                        label = "pill_border"
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = pillBg,
                        border = BorderStroke(1.dp, pillBorder),
                        modifier = Modifier.bouncingClickable(scaleDown = 0.94f) {
                            selectedStatus = statusKey
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                ),
                                color = if (isSelected) ElectricMint else Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Task Count Badge
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) ElectricMint else Color.White.copy(alpha = 0.1f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) Color(0xFF080B11) else Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task List for Selected Kanban Lane
            val filteredTasks = tasks.filter { it.status.name == selectedStatus }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No Tasks Here",
                        description = "There are no tasks in '${statuses.firstOrNull { it.first == selectedStatus }?.second}'. Tap '+ New Task' to create one!"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTasks, key = { it.id.ifBlank { it.hashCode().toString() } }) { task ->
                        KanbanTaskCard(
                            task = task,
                            projectId = projectId,
                            onStatusUpdate = onStatusUpdate
                        )
                    }
                }
            }
        }

        // Create Task Modal Dialog
        if (showCreateDialog) {
            EnhancedCreateTaskDialog(
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
fun KanbanTaskCard(
    task: Task,
    projectId: String,
    onStatusUpdate: (String, String) -> Unit
) {
    var showAiSolution by remember { mutableStateOf(false) }
    var aiSolutionText by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiAiService() }

    val priorityColor = when (task.priority.name) {
        "LOW" -> Color(0xFF64748B)
        "MEDIUM" -> PrimaryIndigoLight
        "HIGH" -> Color(0xFFF59E0B)
        "URGENT" -> StatusAbandoned
        else -> Color.Gray
    }

    val cardBorder = Brush.linearGradient(
        listOf(
            priorityColor.copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF10192A))
            .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column {
            // Task Header: Title & Priority Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatusChip(status = task.priority.name, color = priorityColor)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task Meta Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Assignee Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.assignee?.ifBlank { "Unassigned" } ?: "Unassigned",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Skill Tag
                if (task.skill.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryIndigo.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "#${task.skill}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryIndigoLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gemini AI Code Suggestion Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryIndigo.copy(alpha = 0.10f))
                    .border(BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                    .bouncingClickable(scaleDown = 0.96f) {
                        showAiSolution = !showAiSolution
                        if (showAiSolution && aiSolutionText == null) {
                            isLoadingAi = true
                            coroutineScope.launch {
                                val res = geminiService.generateTaskSolution(
                                    taskTitle = task.title,
                                    skill = task.skill.ifBlank { "Kotlin" },
                                    projectName = projectId
                                )
                                aiSolutionText = res.getOrDefault("// ScrapAI Solution:\nfun solve() { /* implementation */ }")
                                isLoadingAi = false
                            }
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ElectricMint
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showAiSolution) "Hide AI Code Solution" else "Ask Gemini AI For Solution",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = ElectricMint
                    )
                }
            }

            // AI Code Suggestion Box (uses CodeBlockBubble for syntax highlighting!)
            AnimatedVisibility(visible = showAiSolution) {
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                ) {
                    if (isLoadingAi) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ElectricMint
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Gemini is writing code solution...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        CodeBlockBubble(
                            language = "KOTLIN",
                            code = aiSolutionText ?: ""
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Kanban Lane Quick-Action Button
            when (task.status.name) {
                "TODO" -> {
                    Scrap2StackButton(
                        text = "Start Task ➔",
                        onClick = { onStatusUpdate(task.id, "IN_PROGRESS") },
                        modifier = Modifier.height(44.dp)
                    )
                }
                "IN_PROGRESS" -> {
                    Scrap2StackOutlinedButton(
                        text = "Submit for Review ➔",
                        onClick = { onStatusUpdate(task.id, "REVIEW") },
                        modifier = Modifier.height(44.dp)
                    )
                }
                "REVIEW" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(listOf(ElectricMint, RevivalEmerald))
                            )
                            .bouncingClickable(scaleDown = 0.96f) {
                                onStatusUpdate(task.id, "COMPLETED")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFF080B11),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Approve & Complete (+10 Charms)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                ),
                                color = Color(0xFF080B11)
                            )
                        }
                    }
                }
                "COMPLETED" -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = ElectricMint.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, ElectricMint.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ElectricMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Completed (+10 Charms Earned)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricMint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedCreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, TaskPriority) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Workspace Task",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.6f))
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Implement Compose UI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricMint,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    )
                )

                OutlinedTextField(
                    value = skill,
                    onValueChange = { skill = it },
                    label = { Text("Required Skill (e.g. Kotlin, Docker)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricMint,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                    )
                )

                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.8f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.entries.forEach { p ->
                        val isSelected = selectedPriority == p
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricMint.copy(alpha = 0.15f) else Color(0xFF131D31),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ElectricMint.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .bouncingClickable(scaleDown = 0.94f) { selectedPriority = p }
                        ) {
                            Text(
                                text = p.name,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) ElectricMint else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Scrap2StackButton(
                    text = "Create Task",
                    onClick = { onCreate(title, skill, selectedPriority) },
                    enabled = title.isNotBlank()
                )
            }
        }
    }
}
