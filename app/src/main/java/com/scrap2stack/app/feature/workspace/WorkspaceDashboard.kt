package com.scrap2stack.app.feature.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.scrap2stack.app.core.ui.components.Scrap2StackCard
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.core.ui.components.bouncingClickable
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.Task
import com.scrap2stack.app.domain.service.ProjectHealthStatus
import com.scrap2stack.app.domain.service.RevivalVelocityEngine
import com.scrap2stack.app.ui.theme.*

@Composable
fun WorkspaceDashboard(
    project: Project? = null,
    tasks: List<Task> = emptyList(),
    tasksCount: Int = 0,
    completedTasksCount: Int = 0,
    membersCount: Int = 0,
    isShipped: Boolean = false,
    onNavigateToMatches: () -> Unit = {},
    onShipProject: () -> Unit = {}
) {
    val effectiveTasksCount = if (tasks.isNotEmpty()) tasks.size else tasksCount
    val effectiveCompletedTasksCount = if (tasks.isNotEmpty()) tasks.count { it.isCompleted } else completedTasksCount
    val progressPercent = if (effectiveTasksCount > 0) (effectiveCompletedTasksCount * 100) / effectiveTasksCount else 0
    val velocityMetrics = remember(tasks, project, isShipped) {
        RevivalVelocityEngine.calculateMetrics(tasks, project, isShipped)
    }
    var showShipConfirmationDialog by remember { mutableStateOf(false) }
    var showCelebrationDialog by remember { mutableStateOf(false) }

    // Celebration Dialog when project is shipped
    if (showCelebrationDialog) {
        Dialog(onDismissRequest = { showCelebrationDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(ElectricMint, AmberGold)))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(AmberGold.copy(alpha = 0.3f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚀", fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Project Shipped!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Congratulations! Your project '${project?.name ?: "Scrap2Stack"}' has been marked as completed and published to the Community Shipped Stacks showcase.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AmberGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+100 Charms Added to Your Reputation",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = AmberGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showCelebrationDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricMint, contentColor = Color(0xFF0F172A))
                    ) {
                        Text("Continue In Workspace", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Confirmation Dialog before shipping
    if (showShipConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showShipConfirmationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚢", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ship this Project?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "This will mark '${project?.name ?: "this project"}' as COMPLETED, publish it to the Community Shipped Stacks showcase, and award +100 Charms to you and your collaborators. Are you ready to ship?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShipConfirmationDialog = false
                        onShipProject()
                        showCelebrationDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Yes, Ship Stack!", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShipConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(title = "Project Overview")

        // Progress Gauge Card
        Scrap2StackCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Revival Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$effectiveCompletedTasksCount of $effectiveTasksCount tasks completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isShipped) ElectricMint else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { if (effectiveTasksCount > 0) effectiveCompletedTasksCount.toFloat() / effectiveTasksCount else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    strokeCap = StrokeCap.Round,
                    color = if (isShipped) ElectricMint else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Velocity & Burnup Analytics Card (Data Science Engine)
        Scrap2StackCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = ElectricMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Live Velocity & Burnup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Predictive delivery analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Health Status Pill
                    val (healthBg, healthBorder, healthTextColor) = when (velocityMetrics.healthStatus) {
                        ProjectHealthStatus.ACCELERATING -> Triple(ElectricMint.copy(alpha = 0.15f), ElectricMint.copy(alpha = 0.4f), ElectricMint)
                        ProjectHealthStatus.ON_TRACK -> Triple(RevivalEmerald.copy(alpha = 0.15f), RevivalEmerald.copy(alpha = 0.4f), ElectricMint)
                        ProjectHealthStatus.AT_RISK_BOTTLENECK -> Triple(AmberGold.copy(alpha = 0.15f), AmberGold.copy(alpha = 0.4f), AmberGold)
                        ProjectHealthStatus.STALLED -> Triple(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), MaterialTheme.colorScheme.error.copy(alpha = 0.4f), MaterialTheme.colorScheme.error)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = healthBg,
                        border = BorderStroke(1.dp, healthBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(healthTextColor)
                            )
                            Text(
                                text = velocityMetrics.healthBadgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = healthTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Metric Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Story Points Metric
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Burnup Points",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${velocityMetrics.completedStoryPoints}/${velocityMetrics.totalStoryPoints}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "${velocityMetrics.progressPercent}% burned",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricMint
                            )
                        }
                    }

                    // Velocity Metric
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Cadence",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${velocityMetrics.weeklyVelocityStoryPoints}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "pts / week",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold
                            )
                        }
                    }

                    // Ship Date Forecast
                    Surface(
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF131D31),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Est. Ship",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isShipped) "Shipped 🚀" else "~${velocityMetrics.estimatedWeeksToShip} wks",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isShipped) ElectricMint else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isShipped) "Live stack" else "80% CI: ${velocityMetrics.confidenceIntervalWeeks.first}-${velocityMetrics.confidenceIntervalWeeks.second}w",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Bottleneck & Risk Advisory Banner
                if (!velocityMetrics.bottleneckInsight.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (velocityMetrics.healthStatus) {
                            ProjectHealthStatus.AT_RISK_BOTTLENECK, ProjectHealthStatus.STALLED -> AmberGold.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when (velocityMetrics.healthStatus) {
                                ProjectHealthStatus.AT_RISK_BOTTLENECK, ProjectHealthStatus.STALLED -> AmberGold.copy(alpha = 0.35f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (velocityMetrics.healthStatus) {
                                    ProjectHealthStatus.AT_RISK_BOTTLENECK -> Icons.Default.WarningAmber
                                    ProjectHealthStatus.STALLED -> Icons.Default.Speed
                                    ProjectHealthStatus.ACCELERATING -> Icons.Default.RocketLaunch
                                    else -> Icons.Default.CheckCircle
                                },
                                contentDescription = null,
                                tint = when (velocityMetrics.healthStatus) {
                                    ProjectHealthStatus.AT_RISK_BOTTLENECK, ProjectHealthStatus.STALLED -> AmberGold
                                    else -> ElectricMint
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = velocityMetrics.bottleneckInsight,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Shipped Status Banner OR Shipping CTA Card
        if (isShipped) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF13221C),
                border = BorderStroke(1.dp, ElectricMint.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ElectricMint.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = ElectricMint, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Shipped Stack Showcase Live",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricMint
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "This repository has been successfully revived! It is actively showcased in the Community Stacks feed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        } else {
            // Ship Stack Call-to-Action Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncingClickable(scaleDown = 0.98f) { showShipConfirmationDialog = true },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E1736),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(PrimaryIndigo, AmberGold)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚢", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ready to Ship this Stack?",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Publish to Community Showcase & earn +100 Charms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showShipConfirmationDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ship Project (+100 Charms)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Total Tasks",
                value = tasksCount.toString(),
                icon = Icons.Default.TaskAlt,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Team Members",
                value = membersCount.toString(),
                icon = Icons.Default.Group,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Recommendation Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ScrapAI Workspace Tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Moving tasks to 'Completed' in the Kanban board immediately awards +10 Charms to your reputation ledger and advances your project revival score!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToMatches,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invite Developer Matches", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity
        SectionHeader(title = "Recent Workspace Activity")

        ActivityItem(
            icon = Icons.Default.CheckCircle,
            text = if (isShipped) "Project marked as COMPLETED and published to Showcase" else "Workspace synchronized with ScrapAI roadmap",
            time = "Recent",
            color = if (isShipped) ElectricMint else MaterialTheme.colorScheme.secondary
        )
        ActivityItem(
            icon = Icons.Default.Person,
            text = "Active project maintainers synchronized",
            time = "Recent",
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Scrap2StackCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun ActivityItem(icon: ImageVector, text: String, time: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}
