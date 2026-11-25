package com.scrap2stack.app.feature.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackButton
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.core.ui.components.SkillChip
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.service.ContributionVerificationEngine
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.NeonCyan
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val brutal = MaterialTheme.brutalColors

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = brutal.canvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dotGridBackground()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    LoadingView(modifier = Modifier.padding(20.dp))
                }
                is ProfileUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadProfile() }
                    )
                }
                is ProfileUiState.Success -> {
                    ProfileContent(
                        developer = state.developer,
                        contributions = state.contributions,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToEditProfile = onNavigateToEditProfile
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProfileContent(
    developer: Developer,
    contributions: List<CharmContribution>,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Neo-Brutalist Hero Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = ElectricMint,
                    shadowOffsetX = 5.dp,
                    shadowOffsetY = 5.dp
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(
                        name = developer.name.ifBlank { "User" },
                        modifier = Modifier.size(68.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = developer.name.ifBlank { "Developer" },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 21.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = brutal.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Angled Sticker Rank Badge
                            val rankBadge = when {
                                developer.charms >= 200 -> "MASTER"
                                developer.experienceLevel.name.equals("ADVANCED", ignoreCase = true) -> "PRO"
                                else -> "DEV"
                            }
                            Box(
                                modifier = Modifier
                                    .graphicsLayer { rotationZ = -3f }
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberGold)
                                    .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rankBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = BrutalInkBlack
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (developer.username.isNotBlank()) "@${developer.username}" else "@developer",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = brutal.accentOnSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = Color.White,
                                borderColor = BrutalShadowBlack,
                                borderWidth = 2.dp,
                                shadowColor = ElectricMint,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = onNavigateToSettings
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = BrutalInkBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (developer.bio.isNotBlank()) developer.bio else "Passionate developer revamping open source side projects.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        fontSize = 14.sp
                    ),
                    color = brutal.textPrimary.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Scrap2StackButton(
                    text = "EDIT PROFILE →",
                    onClick = onNavigateToEditProfile
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Tactile Stat Blocks (Charms & Rank)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileStatCard(
                label = "CHARMS",
                value = "${developer.charms} ★",
                containerColor = brutal.surface,
                borderColor = brutal.border,
                shadowColor = PrimaryIndigo,
                labelColor = brutal.textMuted,
                valueColor = brutal.accentOnSurface,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                label = "RANK",
                value = developer.experienceLevel.name,
                containerColor = PrimaryIndigo,
                borderColor = BrutalShadowBlack,
                shadowColor = ElectricMint,
                labelColor = Color.White.copy(alpha = 0.85f),
                valueColor = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 21-Day Revival Activity Heatmap
        RevivalHeatmapCard(contributions = contributions)

        // Bayesian Verified Skill Confidence
        Spacer(modifier = Modifier.height(18.dp))
        SectionHeader(title = "Skill Telemetry")
        if (developer.skills.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                developer.skills.forEach { skill ->
                    val metric = ContributionVerificationEngine.getSkillConfidence(developer, skill)
                    SkillChip(skill = skill, isVerified = metric.isVerified)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            SkillConfidenceCard(developer = developer)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .brutalCard(
                        cornerRadius = 14.dp,
                        containerColor = brutal.surface,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "Add your tech stack in EDIT PROFILE → to track Bayesian verification confidence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = brutal.textMuted
                )
            }
        }

        // Interests Section
        if (developer.interests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Interests")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                developer.interests.forEach { interest ->
                    Box(
                        modifier = Modifier.brutalCard(
                            cornerRadius = 10.dp,
                            containerColor = brutal.surfaceRaised,
                            borderColor = brutal.border,
                            borderWidth = 1.5.dp,
                            shadowColor = brutal.shadow,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp
                        )
                    ) {
                        Text(
                            text = interest,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = brutal.textPrimary
                        )
                    }
                }
            }
        }

        // Social Links Section
        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Social & Links")

        if (developer.githubUrl.isNotBlank()) {
            SocialLinkItem(
                icon = Icons.Default.Code,
                label = "GitHub",
                value = developer.githubUrl,
                accentColor = NeonCyan,
                actionFill = Color.White
            )
        }
        if (developer.linkedinUrl.isNotBlank()) {
            SocialLinkItem(
                icon = Icons.Default.Link,
                label = "LinkedIn",
                value = developer.linkedinUrl,
                accentColor = AmberGold,
                actionFill = AmberGold
            )
        }
        if (developer.portfolioUrl.isNotBlank()) {
            SocialLinkItem(
                icon = Icons.Default.Public,
                label = "Portfolio",
                value = developer.portfolioUrl,
                accentColor = ElectricMint,
                actionFill = ElectricMint
            )
        }

        if (developer.githubUrl.isBlank() && developer.linkedinUrl.isBlank() && developer.portfolioUrl.isBlank()) {
            Text(
                text = "No links added yet. Tap 'EDIT PROFILE →' to add links.",
                style = MaterialTheme.typography.bodySmall,
                color = brutal.textMuted
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun RevivalHeatmapCard(contributions: List<CharmContribution>) {
    val brutal = MaterialTheme.brutalColors
    val levels = buildHeatmapLevels(contributions = contributions, days = 21)
    val activeDays = levels.count { it > 0 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 16.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
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
                Text(
                    text = "21-DAY REVIVAL HEATMAP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = brutal.textPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricMint)
                        .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$activeDays ACTIVE DAYS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        ),
                        color = BrutalInkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            levels.chunked(7).forEachIndexed { rowIdx, weekRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weekRow.forEach { level ->
                        val cellColor = when (level) {
                            0 -> brutal.surfaceRaised
                            1 -> ElectricMint.copy(alpha = 0.30f)
                            2 -> ElectricMint.copy(alpha = 0.55f)
                            3 -> ElectricMint.copy(alpha = 0.80f)
                            else -> ElectricMint
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(cellColor)
                                .border(1.5.dp, brutal.border.copy(alpha = if (level == 0) 0.45f else 0.9f), RoundedCornerShape(5.dp))
                        )
                    }
                }
                if (rowIdx < 2) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun SkillConfidenceCard(developer: Developer) {
    val brutal = MaterialTheme.brutalColors
    val trackedSkills = developer.skills.take(4)

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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "BAYESIAN VERIFICATION CONFIDENCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.6.sp
                ),
                color = brutal.textMuted
            )

            trackedSkills.forEach { skill ->
                val metric = ContributionVerificationEngine.getSkillConfidence(developer, skill)
                val percent = (metric.confidence * 100).toInt().coerceIn(5, 100)
                val barColor = if (metric.isVerified) ElectricMint else PrimaryIndigo

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = skill,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = brutal.textPrimary
                        )
                        Text(
                            text = "$percent% • ${metric.confidenceLevelLabel.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (metric.isVerified) brutal.accentOnSurface else brutal.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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
                                .fillMaxHeight()
                                .fillMaxWidth(metric.confidence.toFloat().coerceIn(0.08f, 1f))
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Buckets the last [days] of charm contributions into 0..4 heatmap intensity levels.
 */
internal fun buildHeatmapLevels(
    contributions: List<CharmContribution>,
    days: Int = 21,
    todayMillis: Long = System.currentTimeMillis()
): List<Int> {
    if (days <= 0) return emptyList()

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val countsByDate = mutableMapOf<String, Int>()
    contributions.forEach { entry ->
        val dayKey = entry.createdAt.trim().take(10)
        if (dayKey.length == 10 && dayKey[4] == '-' && dayKey[7] == '-') {
            countsByDate[dayKey] = (countsByDate[dayKey] ?: 0) + 1
        }
    }

    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = todayMillis
        add(Calendar.DAY_OF_YEAR, -(days - 1))
    }

    return List(days) {
        val key = dateFormatter.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        when (val count = countsByDate[key] ?: 0) {
            0 -> 0
            1 -> 1
            2 -> 2
            in 3..4 -> 3
            else -> 4
        }
    }
}

@Composable
fun ProfileStatCard(
    label: String,
    value: String,
    containerColor: Color = MaterialTheme.brutalColors.surface,
    borderColor: Color = MaterialTheme.brutalColors.border,
    shadowColor: Color = PrimaryIndigo,
    labelColor: Color = MaterialTheme.brutalColors.textMuted,
    valueColor: Color = MaterialTheme.brutalColors.accentOnSurface,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .brutalCard(
                cornerRadius = 14.dp,
                containerColor = containerColor,
                borderColor = borderColor,
                borderWidth = 2.dp,
                shadowColor = shadowColor,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .padding(horizontal = 14.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                ),
                color = labelColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                ),
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SocialLinkItem(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color = ElectricMint,
    actionFill: Color = Color.White
) {
    val uriHandler = LocalUriHandler.current
    val displayHandle = formatSocialDisplay(label = label, rawUrl = value)

    val openUrl = {
        if (value.isNotBlank()) {
            val formattedUrl = if (!value.startsWith("http://") && !value.startsWith("https://")) {
                "https://$value"
            } else {
                value
            }
            try {
                uriHandler.openUri(formattedUrl)
            } catch (e: Exception) {
                Log.e("SocialLinkItem", "Failed to open link: $formattedUrl", e)
            }
        }
    }

    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .tactileBrutalCard(
                cornerRadius = 14.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = accentColor,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp,
                onClick = openUrl
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .brutalCard(
                        cornerRadius = 10.dp,
                        containerColor = accentColor,
                        borderColor = BrutalShadowBlack,
                        borderWidth = 1.5.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 2.dp,
                        shadowOffsetY = 2.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = BrutalInkBlack
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = brutal.textMuted
                )
                Text(
                    text = displayHandle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = brutal.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(actionFill)
                    .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "OPEN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    ),
                    color = BrutalInkBlack
                )
            }
        }
    }
}

/**
 * Strips protocol, tracking query parameters (such as UTM tags), and platform path prefixes
 * so social cards show a clean handle while keeping the original URL for click navigation.
 */
internal fun formatSocialDisplay(label: String, rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return ""

    val withoutQuery = trimmed.substringBefore('?').substringBefore('#')
    val withoutScheme = withoutQuery
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
        .trimEnd('/')

    return when {
        label.equals("GitHub", ignoreCase = true) || withoutScheme.startsWith("github.com/", ignoreCase = true) -> {
            withoutScheme.substringAfter("github.com/").ifBlank { withoutScheme }
        }
        label.equals("LinkedIn", ignoreCase = true) || withoutScheme.contains("linkedin.com/in/", ignoreCase = true) -> {
            withoutScheme.substringAfter("linkedin.com/in/").ifBlank {
                withoutScheme.substringAfter("linkedin.com/").ifBlank { withoutScheme }
            }
        }
        else -> withoutScheme
    }
}
