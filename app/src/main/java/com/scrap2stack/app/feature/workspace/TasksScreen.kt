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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.scrap2stack.app.core.network.GeminiAiService
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Task
import com.scrap2stack.app.domain.model.TaskPriority
import com.scrap2stack.app.feature.chat.components.CodeBlockBubble
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.brutalColors
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    projectId: String,
    tasks: List<Task>,
    onCreateTask: (String, String, TaskPriority) -> Unit,
    onStatusUpdate: (String, String) -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    var selectedStatus by remember { mutableStateOf("TODO") }
    val statuses = listOf(
        "TODO" to "To Do",
        "IN_PROGRESS" to "In Progress",
        "REVIEW" to "In Review",
        "COMPLETED" to "Completed"
    )
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brutal.canvas)
            .dotGridBackground()
    ) {
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
                        text = "KANBAN BOARD //",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        ),
                        color = brutal.accentOnSurface
                    )
                    Text(
                        text = "Workspace Tasks",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = brutal.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .tactileBrutalCard(
                            cornerRadius = 10.dp,
                            containerColor = ElectricMint,
                            borderColor = BrutalShadowBlack,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp,
                            onClick = { showCreateDialog = true }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Task",
                            tint = BrutalInkBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NEW TASK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            ),
                            color = BrutalInkBlack
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

                    Box(
                        modifier = Modifier
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = if (isSelected) ElectricMint else brutal.surfaceRaised,
                                borderColor = if (isSelected) BrutalShadowBlack else brutal.border,
                                borderWidth = 1.5.dp,
                                shadowColor = if (isSelected) PrimaryIndigo else brutal.shadow,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = { selectedStatus = statusKey }
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = statusLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) BrutalInkBlack else brutal.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) BrutalInkBlack else ElectricMint)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isSelected) ElectricMint else BrutalInkBlack
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
    var aiIsLiveData by remember { mutableStateOf(true) }
    var aiStatusNote by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiAiService() }

    val brutal = MaterialTheme.brutalColors
    val priorityColor = when (task.priority.name) {
        "LOW" -> Color(0xFF64748B)
        "MEDIUM" -> PrimaryIndigoLight
        "HIGH" -> AmberGold
        "URGENT" -> StatusAbandoned
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = priorityColor,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .padding(16.dp)
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
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = brutal.textPrimary,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(brutal.surfaceRaised)
                            .border(1.dp, brutal.border, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = brutal.textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.assignee?.ifBlank { "Unassigned" } ?: "Unassigned",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = brutal.textMuted
                    )
                }

                if (task.skill.isNotBlank()) {
                    SkillChip(skill = "#${task.skill}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gemini AI Code Suggestion Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tactileBrutalCard(
                        cornerRadius = 10.dp,
                        containerColor = brutal.surfaceRaised,
                        borderColor = PrimaryIndigo,
                        borderWidth = 1.5.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 2.dp,
                        shadowOffsetY = 2.dp,
                        onClick = {
                            showAiSolution = !showAiSolution
                            if (showAiSolution && aiSolutionText == null) {
                                isLoadingAi = true
                                coroutineScope.launch {
                                    val res = geminiService.generateTaskSolution(
                                        taskTitle = task.title,
                                        skill = task.skill.ifBlank { "Kotlin" },
                                        projectName = projectId
                                    )
                                    res.onSuccess { live ->
                                        aiSolutionText = live.data
                                        aiIsLiveData = live.isLiveData
                                        aiStatusNote = live.statusNote
                                    }.onFailure {
                                        aiSolutionText = "// ScrapAI Solution:\nfun solve() { /* implementation */ }"
                                        aiIsLiveData = false
                                        aiStatusNote = "Offline heuristic"
                                    }
                                    isLoadingAi = false
                                }
                            }
                        }
                    )
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
                        modifier = Modifier.size(15.dp),
                        tint = brutal.accentOnSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showAiSolution) "HIDE AI SOLUTION" else "ASK GEMINI AI FOR SOLUTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = brutal.accentOnSurface
                    )
                }
            }

            // AI Code Suggestion Box
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
                                color = brutal.accentOnSurface
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Gemini is writing code solution...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = brutal.textMuted
                            )
                        }
                    } else {
                        if (!aiIsLiveData) {
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BrutalBadge(
                                    text = "OFFLINE HEURISTIC",
                                    containerColor = AmberGold,
                                    contentColor = BrutalInkBlack,
                                    borderColor = brutal.border
                                )
                                if (!aiStatusNote.isNullOrBlank()) {
                                    Text(
                                        text = aiStatusNote ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = brutal.textMuted
                                    )
                                }
                            }
                        }
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
                        text = "START TASK →",
                        onClick = { onStatusUpdate(task.id, "IN_PROGRESS") },
                        modifier = Modifier.height(46.dp)
                    )
                }
                "IN_PROGRESS" -> {
                    Scrap2StackOutlinedButton(
                        text = "SUBMIT FOR REVIEW →",
                        onClick = { onStatusUpdate(task.id, "REVIEW") },
                        modifier = Modifier.height(46.dp)
                    )
                }
                "REVIEW" -> {
                    Scrap2StackButton(
                        text = "APPROVE & COMPLETE (+10 CHARMS)",
                        onClick = { onStatusUpdate(task.id, "COMPLETED") },
                        containerColor = ElectricMint,
                        contentColor = BrutalInkBlack,
                        modifier = Modifier.height(48.dp)
                    )
                }
                "COMPLETED" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .brutalCard(
                                cornerRadius = 10.dp,
                                containerColor = ElectricMint,
                                borderColor = BrutalShadowBlack,
                                borderWidth = 1.5.dp,
                                shadowColor = BrutalShadowBlack,
                                shadowOffsetX = 2.dp,
                                shadowOffsetY = 2.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrutalInkBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COMPLETED (+10 CHARMS EARNED)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = BrutalInkBlack
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
    val brutal = MaterialTheme.brutalColors
    var title by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = BrutalShadowBlack,
                    shadowOffsetX = 5.dp,
                    shadowOffsetY = 5.dp
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        BrutalHeaderTag(text = "KANBAN // NEW")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "New Workspace Task",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = brutal.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = brutal.surfaceRaised,
                                borderColor = brutal.border,
                                borderWidth = 1.5.dp,
                                shadowColor = brutal.shadow,
                                shadowOffsetX = 2.dp,
                                shadowOffsetY = 2.dp,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = brutal.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title (e.g. Implement Compose UI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brutal.border,
                        unfocusedBorderColor = brutal.border.copy(alpha = 0.45f),
                        focusedContainerColor = brutal.surfaceRaised,
                        unfocusedContainerColor = brutal.surfaceRaised,
                        focusedTextColor = brutal.textPrimary,
                        unfocusedTextColor = brutal.textPrimary,
                        focusedLabelColor = brutal.accentOnSurface,
                        unfocusedLabelColor = brutal.textMuted
                    )
                )

                OutlinedTextField(
                    value = skill,
                    onValueChange = { skill = it },
                    label = { Text("Required Skill (e.g. Kotlin, Docker)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brutal.border,
                        unfocusedBorderColor = brutal.border.copy(alpha = 0.45f),
                        focusedContainerColor = brutal.surfaceRaised,
                        unfocusedContainerColor = brutal.surfaceRaised,
                        focusedTextColor = brutal.textPrimary,
                        unfocusedTextColor = brutal.textPrimary,
                        focusedLabelColor = brutal.accentOnSurface,
                        unfocusedLabelColor = brutal.textMuted
                    )
                )

                Text(
                    text = "PRIORITY LEVEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp
                    ),
                    color = brutal.textMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { p ->
                        val isSelected = selectedPriority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .tactileBrutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = if (isSelected) ElectricMint else brutal.surfaceRaised,
                                    borderColor = brutal.border,
                                    borderWidth = if (isSelected) 2.dp else 1.5.dp,
                                    shadowColor = brutal.shadow,
                                    shadowOffsetX = if (isSelected) 3.dp else 2.dp,
                                    shadowOffsetY = if (isSelected) 3.dp else 2.dp,
                                    onClick = { selectedPriority = p }
                                )
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) BrutalInkBlack else brutal.textMuted,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Scrap2StackButton(
                    text = "Create Task",
                    onClick = { onCreate(title, skill, selectedPriority) },
                    enabled = title.isNotBlank()
                )
            }
        }
    }
}
