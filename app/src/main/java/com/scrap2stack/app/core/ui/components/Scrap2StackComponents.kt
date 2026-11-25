package com.scrap2stack.app.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.ui.theme.BrutalBorderWhite
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.BrutalSurfaceDark
import com.scrap2stack.app.ui.theme.BrutalSurfaceRaised
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoDark
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.RevivalEmerald

@Composable
fun Scrap2StackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = ElectricMint,
    contentColor: Color = BrutalInkBlack,
    shadowColor: Color = BrutalShadowBlack
) {
    val activeBg = if (enabled) containerColor else Color(0xFF242B3A)
    val activeBorder = if (enabled) BrutalShadowBlack else Color(0xFF475569)
    val activeText = if (enabled) contentColor else Color(0xFF94A3B8)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .tactileBrutalCard(
                cornerRadius = 14.dp,
                containerColor = activeBg,
                borderColor = activeBorder,
                borderWidth = 2.dp,
                shadowColor = shadowColor,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            ),
            color = activeText
        )
    }
}

@Composable
fun Scrap2StackOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shadowColor: Color = ElectricMint
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .tactileBrutalCard(
                cornerRadius = 14.dp,
                containerColor = BrutalSurfaceRaised,
                borderColor = if (enabled) BrutalBorderWhite else Color(0xFF475569),
                borderWidth = 2.dp,
                shadowColor = if (enabled) shadowColor else BrutalShadowBlack,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 0.4.sp
            ),
            color = if (enabled) ElectricMint else Color.Gray
        )
    }
}

@Composable
fun Scrap2StackCard(
    modifier: Modifier = Modifier,
    containerColor: Color = BrutalSurfaceDark,
    borderColor: Color = BrutalBorderWhite,
    shadowColor: Color = BrutalShadowBlack,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = containerColor,
                borderColor = borderColor,
                borderWidth = 2.dp,
                shadowColor = shadowColor,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
    ) {
        content()
    }
}

@Composable
fun SkillChip(
    skill: String,
    isVerified: Boolean = false
) {
    val bgColor = if (isVerified) ElectricMint else BrutalSurfaceRaised
    val borderColor = if (isVerified) BrutalShadowBlack else BrutalBorderWhite.copy(alpha = 0.8f)
    val textColor = if (isVerified) BrutalInkBlack else Color.White

    Box(
        modifier = Modifier
            .padding(end = 6.dp, bottom = 6.dp)
            .brutalCard(
                cornerRadius = 10.dp,
                containerColor = bgColor,
                borderColor = borderColor,
                borderWidth = 1.5.dp,
                shadowColor = if (isVerified) PrimaryIndigo else BrutalShadowBlack,
                shadowOffsetX = 2.dp,
                shadowOffsetY = 2.dp
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = skill,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = textColor
            )
            if (isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Skill",
                    tint = BrutalInkBlack,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .brutalCard(
                cornerRadius = 10.dp,
                containerColor = BrutalSurfaceRaised,
                borderColor = color,
                borderWidth = 1.5.dp,
                shadowColor = BrutalShadowBlack,
                shadowOffsetX = 2.dp,
                shadowOffsetY = 2.dp
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.4.sp
                ),
                color = color
            )
        }
    }
}

@Composable
fun ScoreCard(label: String, score: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.brutalCard(
            cornerRadius = 14.dp,
            containerColor = BrutalSurfaceDark,
            borderColor = BrutalBorderWhite,
            borderWidth = 2.dp,
            shadowColor = ElectricMint,
            shadowOffsetX = 4.dp,
            shadowOffsetY = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.6.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = score,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = ElectricMint
            )
        }
    }
}

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "U" }

    Box(
        modifier = modifier
            .size(48.dp)
            .brutalCard(
                cornerRadius = 14.dp,
                containerColor = ElectricMint,
                borderColor = BrutalShadowBlack,
                borderWidth = 2.dp,
                shadowColor = BrutalShadowBlack,
                shadowOffsetX = 3.dp,
                shadowOffsetY = 3.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = BrutalInkBlack
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 12.dp, height = 22.dp)
                .brutalCard(
                    cornerRadius = 4.dp,
                    containerColor = ElectricMint,
                    borderColor = BrutalShadowBlack,
                    borderWidth = 1.5.dp,
                    shadowColor = PrimaryIndigo,
                    shadowOffsetX = 2.dp,
                    shadowOffsetY = 2.dp
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.4).sp
            )
        )
    }
}

@Composable
fun ProjectCardSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = BrutalSurfaceDark,
                borderColor = Color(0xFF334155),
                borderWidth = 2.dp,
                shadowColor = BrutalShadowBlack,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .shimmerEffect()
    )
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            ProjectCardSkeleton()
        }
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = BrutalSurfaceDark,
                    borderColor = BrutalBorderWhite,
                    borderWidth = 2.dp,
                    shadowColor = MaterialTheme.colorScheme.error,
                    shadowOffsetX = 5.dp,
                    shadowOffsetY = 5.dp
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .brutalCard(
                            cornerRadius = 14.dp,
                            containerColor = MaterialTheme.colorScheme.error,
                            borderColor = BrutalShadowBlack,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 3.dp,
                            shadowOffsetY = 3.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Something went wrong",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Scrap2StackButton(text = "TRY AGAIN", onClick = onRetry)
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String = "No Data Found",
    description: String = "There is nothing to show here at the moment.",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = BrutalSurfaceDark,
                    borderColor = BrutalBorderWhite,
                    borderWidth = 2.dp,
                    shadowColor = PrimaryIndigo,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp
                )
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .brutalCard(
                        cornerRadius = 14.dp,
                        containerColor = PrimaryIndigo,
                        borderColor = BrutalShadowBlack,
                        borderWidth = 2.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}
