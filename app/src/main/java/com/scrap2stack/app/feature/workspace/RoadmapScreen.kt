package com.scrap2stack.app.feature.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.scrap2stack.app.core.ui.components.BrutalBadge
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.RoadmapItem
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors
import kotlinx.coroutines.launch

@Composable
fun RoadmapScreen(
    projectId: String,
    roadmapItems: List<RoadmapItem>,
    isGenerating: Boolean = false,
    isLiveData: Boolean = true,
    statusNote: String? = null,
    onGenerateRoadmap: () -> Unit = {},
    onStatusChange: (itemId: String, newStatus: String) -> Unit = { _, _ -> },
    onConvertPhaseToTasks: (RoadmapItem) -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val totalPhases = roadmapItems.size
    val completedPhases = roadmapItems.count { it.status.uppercase() == "COMPLETED" }
    val progressFraction = if (totalPhases > 0) completedPhases.toFloat() / totalPhases.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "roadmap_progress")

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
            Spacer(modifier = Modifier.height(14.dp))

            // AI Engine Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .brutalCard(
                        cornerRadius = 16.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = PrimaryIndigo,
                        shadowOffsetX = 4.dp,
                        shadowOffsetY = 4.dp
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .brutalCard(
                                        cornerRadius = 10.dp,
                                        containerColor = ElectricMint,
                                        borderColor = BrutalShadowBlack,
                                        borderWidth = 1.5.dp,
                                        shadowColor = BrutalShadowBlack,
                                        shadowOffsetX = 2.dp,
                                        shadowOffsetY = 2.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BrutalInkBlack,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Revival Roadmap",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = brutal.textPrimary
                                    )
                                )
                                Text(
                                    text = "GEMINI SPRINT PLANNER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = brutal.accentOnSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Generate/Regenerate Button
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
                                    enabled = !isGenerating,
                                    onClick = onGenerateRoadmap
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGenerating) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = BrutalInkBlack
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "BUILDING...",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            color = BrutalInkBlack,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (roadmapItems.isEmpty()) Icons.Default.AutoAwesome else Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = BrutalInkBlack,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (roadmapItems.isEmpty()) "GENERATE" else "REGEN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            color = BrutalInkBlack,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (!isLiveData && roadmapItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BrutalBadge(
                                text = "OFFLINE HEURISTIC",
                                containerColor = AmberGold,
                                contentColor = BrutalInkBlack,
                                borderColor = brutal.border
                            )
                            if (!statusNote.isNullOrBlank()) {
                                Text(
                                    text = statusNote,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = brutal.textMuted
                                )
                            }
                        }
                    }

                    if (totalPhases > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Milestone Progress: $completedPhases / $totalPhases Phases",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = brutal.textMuted
                                )
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = brutal.accentOnSurface,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(brutal.surfaceRaised)
                                .border(1.5.dp, brutal.border, RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .background(ElectricMint)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (roadmapItems.isEmpty() && !isGenerating) {
                // Empty State with Stack Pop Prompt
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
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
                            .padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .brutalCard(
                                    cornerRadius = 16.dp,
                                    containerColor = ElectricMint,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = brutal.shadow,
                                    shadowOffsetX = 3.dp,
                                    shadowOffsetY = 3.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BrutalInkBlack,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Roadmap Generated Yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = brutal.textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap 'Generate AI Plan' to let Gemini evaluate the codebase and generate an actionable revival trajectory.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = brutal.textMuted
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .tactileBrutalCard(
                                    cornerRadius = 14.dp,
                                    containerColor = ElectricMint,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = BrutalShadowBlack,
                                    shadowOffsetX = 4.dp,
                                    shadowOffsetY = 4.dp,
                                    onClick = onGenerateRoadmap
                                )
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrutalInkBlack, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Generate AI Roadmap with Gemini",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = BrutalInkBlack,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(roadmapItems.sortedBy { it.order }, key = { it.id.ifEmpty { it.order.toString() } }) { item ->
                        RoadmapPhaseCard(
                            item = item,
                            onStatusToggle = {
                                val nextStatus = when (item.status.uppercase()) {
                                    "TODO", "PLANNED" -> "IN_PROGRESS"
                                    "IN_PROGRESS" -> "COMPLETED"
                                    "COMPLETED" -> "TODO"
                                    else -> "TODO"
                                }
                                onStatusChange(item.id, nextStatus)
                            },
                            onConvertToTasks = {
                                onConvertPhaseToTasks(item)
                                snackbarMessage = "Added Phase ${item.order} tasks to Kanban board!"
                            }
                        )
                    }
                }
            }
        }

        // Animated Snackbar Notification
        AnimatedVisibility(
            visible = snackbarMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            snackbarMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .brutalCard(
                            cornerRadius = 14.dp,
                            containerColor = brutal.surface,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = brutal.accentOnSurface, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.bodySmall.copy(color = brutal.textPrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = brutal.textMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoadmapPhaseCard(
    item: RoadmapItem,
    onStatusToggle: () -> Unit,
    onConvertToTasks: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val isCompleted = item.status.uppercase() == "COMPLETED"
    val isInProgress = item.status.uppercase() == "IN_PROGRESS"

    val statusColor by animateColorAsState(
        targetValue = when {
            isCompleted -> ElectricMint
            isInProgress -> AmberGold
            else -> Color(0xFF94A3B8)
        },
        label = "status_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = if (isCompleted) ElectricMint else PrimaryIndigo,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Phase Tag + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryIndigo)
                            .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PHASE ${item.order}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    if (item.milestone) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Milestone",
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Interactive Status Pill
                Box(
                    modifier = Modifier
                        .tactileBrutalCard(
                            cornerRadius = 10.dp,
                            containerColor = statusColor,
                            borderColor = BrutalShadowBlack,
                            borderWidth = 1.5.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp,
                            onClick = onStatusToggle
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isCompleted -> Icons.Default.CheckCircle
                                isInProgress -> Icons.Default.PlayArrow
                                else -> Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = BrutalInkBlack,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.status.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = BrutalInkBlack,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = brutal.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = brutal.textMuted,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Skills and Effort Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.requiredSkills.take(3).forEach { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(brutal.surfaceRaised)
                                .border(1.dp, brutal.border, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = skill,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = brutal.textPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (item.estimatedEffort.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = brutal.textMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.estimatedEffort,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = brutal.textMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Action to convert phase to tasks
                Box(
                    modifier = Modifier
                        .tactileBrutalCard(
                            cornerRadius = 10.dp,
                            containerColor = ElectricMint,
                            borderColor = BrutalShadowBlack,
                            borderWidth = 1.5.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp,
                            onClick = onConvertToTasks
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create tasks",
                            tint = BrutalInkBlack,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "KANBAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = BrutalInkBlack,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
