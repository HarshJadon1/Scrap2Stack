package com.scrap2stack.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.NeonCyan
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.RevivalEmerald
import com.scrap2stack.app.ui.theme.RevivalEmeraldLight
import com.scrap2stack.app.ui.theme.StatusAbandoned
import com.scrap2stack.app.ui.theme.StatusCompleted
import com.scrap2stack.app.ui.theme.StatusReviving

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var upvoteCount by remember(project.id) {
        val base = ((project.revivalScore * 7) % 35) + 12
        mutableIntStateOf(base)
    }
    var hasUpvoted by remember(project.id) { mutableStateOf(false) }

    Scrap2StackCard(
        modifier = modifier
            .padding(vertical = 6.dp)
            .bouncingClickable(scaleDown = 0.98f, onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row: Project Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.3).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (project.status == ProjectStatus.COMPLETED) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFBBF24).copy(alpha = 0.2f))
                                    .border(0.5.dp, Color(0xFFFBBF24).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🏆 SHIPPED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFBBF24),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.technologies.take(4).joinToString("  •  "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = ElectricMint.copy(alpha = 0.85f)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                val statusColor = when (project.status) {
                    ProjectStatus.IDEA -> PrimaryIndigoLight
                    ProjectStatus.INCOMPLETE -> Color(0xFFF59E0B)
                    ProjectStatus.ABANDONED -> StatusAbandoned
                    ProjectStatus.PAUSED -> Color(0xFF94A3B8)
                    ProjectStatus.MVP_INCOMPLETE -> ElectricMint
                    ProjectStatus.REVIVING -> StatusReviving
                    ProjectStatus.COMPLETED -> StatusCompleted
                    ProjectStatus.INACTIVE -> Color(0xFF64748B)
                }
                StatusChip(status = project.status.name, color = statusColor)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Project Description
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )

            // Required Skills Badges
            if (project.requiredSkills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Start
                ) {
                    project.requiredSkills.take(4).forEach { skill ->
                        SkillChip(skill = skill)
                    }
                    if (project.requiredSkills.size > 4) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "+${project.requiredSkills.size - 4}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.07f)
            )

            // Footer: Revival Meter & Last Active Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Revival Potential Gauge
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = ElectricMint
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Revival Potential",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${project.revivalScore}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            ),
                            color = ElectricMint
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((project.revivalScore / 100f).coerceIn(0.05f, 1f))
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(RevivalEmerald, ElectricMint)
                                        )
                                    )
                            )
                        }
                    }
                }
                
                // Upvote & Last Active info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (hasUpvoted) Color(0xFFEC4899).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                            .border(
                                1.dp,
                                if (hasUpvoted) Color(0xFFEC4899) else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(12.dp)
                            )
                            .bouncingClickable {
                                hasUpvoted = !hasUpvoted
                                upvoteCount += if (hasUpvoted) 1 else -1
                            }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasUpvoted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Upvote",
                                tint = if (hasUpvoted) Color(0xFFEC4899) else Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$upvoteCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (hasUpvoted) Color(0xFFEC4899) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Last Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project.lastActivity.ifBlank { "Recently" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}
