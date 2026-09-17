package com.scrap2stack.app.feature.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.Scrap2StackCard
import com.scrap2stack.app.core.ui.components.SectionHeader

@Composable
fun WorkspaceDashboard(
    tasksCount: Int = 0,
    completedTasksCount: Int = 0,
    membersCount: Int = 0,
    onNavigateToMatches: () -> Unit = {}
) {
    val progressPercent = if (tasksCount > 0) (completedTasksCount * 100) / tasksCount else 0

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
                            text = "$completedTasksCount of $tasksCount tasks completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { if (tasksCount > 0) completedTasksCount.toFloat() / tasksCount else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
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
                    text = "Completing tasks in this workspace automatically awards +10 Charms to your reputation ledger and advances your project revival score!",
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
            text = "Workspace created & initialized with ScrapAI roadmap",
            time = "Today",
            color = MaterialTheme.colorScheme.secondary
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
