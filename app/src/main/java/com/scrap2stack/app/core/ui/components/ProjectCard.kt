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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.domain.model.ProjectStatus
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalBorderWhite
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.BrutalSurfaceDark
import com.scrap2stack.app.ui.theme.BrutalSurfaceRaised
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .tactileBrutalCard(
                cornerRadius = 16.dp,
                containerColor = BrutalSurfaceDark,
                borderColor = BrutalBorderWhite,
                borderWidth = 2.dp,
                shadowColor = if (project.status == ProjectStatus.COMPLETED) ElectricMint else PrimaryIndigo,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
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
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
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
                                    .graphicsLayer { rotationZ = -2f }
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberGold)
                                    .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SHIPPED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = BrutalInkBlack,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.technologies.take(4).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = ElectricMint
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                val statusColor = when (project.status) {
                    ProjectStatus.IDEA -> PrimaryIndigoLight
                    ProjectStatus.INCOMPLETE -> AmberGold
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
                        Box(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .brutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = BrutalSurfaceRaised,
                                    borderColor = Color(0xFF475569),
                                    borderWidth = 1.5.dp,
                                    shadowColor = BrutalShadowBlack,
                                    shadowOffsetX = 2.dp,
                                    shadowOffsetY = 2.dp
                                )
                        ) {
                            Text(
                                text = "+${project.requiredSkills.size - 4}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.5.dp,
                color = Color.White.copy(alpha = 0.14f)
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
                            text = "REVIVAL SCORE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
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
                                .width(76.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(BrutalSurfaceRaised)
                                .border(1.5.dp, BrutalBorderWhite.copy(alpha = 0.7f), RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((project.revivalScore / 100f).coerceIn(0.08f, 1f))
                                    .background(ElectricMint)
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
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = if (hasUpvoted) Color(0xFFEC4899) else BrutalSurfaceRaised,
                                borderColor = if (hasUpvoted) BrutalShadowBlack else BrutalBorderWhite,
                                borderWidth = 1.5.dp,
                                shadowColor = BrutalShadowBlack,
                                shadowOffsetX = 2.dp,
                                shadowOffsetY = 2.dp,
                                onClick = {
                                    hasUpvoted = !hasUpvoted
                                    upvoteCount += if (hasUpvoted) 1 else -1
                                }
                            )
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasUpvoted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Upvote",
                                tint = if (hasUpvoted) BrutalInkBlack else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$upvoteCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (hasUpvoted) BrutalInkBlack else Color.White,
                                    fontWeight = FontWeight.Black,
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
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = project.lastActivity.ifBlank { "Recently" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
