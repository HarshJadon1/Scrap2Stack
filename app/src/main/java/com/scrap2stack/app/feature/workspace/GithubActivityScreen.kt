package com.scrap2stack.app.feature.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.network.GitHubService
import com.scrap2stack.app.core.ui.components.BrutalBadge
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.data.remote.dto.GitHubContributionDto
import com.scrap2stack.app.domain.model.GitHubRepoInfo
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors
import kotlinx.coroutines.launch

@Composable
fun GithubActivityScreen(
    projectId: String,
    repoUrl: String = "https://github.com/Scrap2Stack/revival-engine",
    contributions: List<GitHubContributionDto> = emptyList(),
    onSync: () -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
    val gitHubService = remember { GitHubService() }
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var repoInfo by remember { mutableStateOf<GitHubRepoInfo?>(null) }
    var commitFeed by remember { mutableStateOf<List<GitHubContributionDto>>(contributions) }
    var isLiveRepoData by remember { mutableStateOf(true) }
    var repoStatusNote by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val parsedRepo = remember(repoUrl) {
        gitHubService.parseRepoUrl(repoUrl) ?: Pair("Scrap2Stack", "revival-engine")
    }

    LaunchedEffect(projectId, repoUrl) {
        isSyncing = true
        coroutineScope.launch {
            val infoResult = gitHubService.fetchRepoInfo(parsedRepo.first, parsedRepo.second)
            infoResult.onSuccess { live ->
                repoInfo = live.data
                isLiveRepoData = live.isLiveData
                repoStatusNote = live.statusNote
            }

            val commitsResult = gitHubService.fetchRecentCommits(parsedRepo.first, parsedRepo.second, projectId)
            commitsResult.onSuccess { live ->
                commitFeed = live.data
                if (!live.isLiveData) {
                    isLiveRepoData = false
                    repoStatusNote = live.statusNote ?: repoStatusNote
                }
            }
            isSyncing = false
        }
    }

    val filteredList = remember(commitFeed, selectedFilter) {
        when (selectedFilter) {
            "COMMITS" -> commitFeed.filter { it.type == "COMMIT" }
            "PRS" -> commitFeed.filter { it.type.contains("PULL_REQUEST") }
            "ISSUES" -> commitFeed.filter { it.type.contains("ISSUE") }
            else -> commitFeed
        }
    }

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

            // Repo Overview Header Card
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = BrutalInkBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${parsedRepo.first}/${parsedRepo.second}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = brutal.textPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Branch: ${repoInfo?.defaultBranch ?: "master"} • ${repoInfo?.primaryLanguage ?: "Kotlin"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = brutal.accentOnSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Live Sync Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .tactileBrutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = brutal.surfaceRaised,
                                    borderColor = brutal.border,
                                    borderWidth = 1.5.dp,
                                    shadowColor = ElectricMint,
                                    shadowOffsetX = 2.dp,
                                    shadowOffsetY = 2.dp,
                                    enabled = !isSyncing,
                                    onClick = {
                                        isSyncing = true
                                        onSync()
                                        coroutineScope.launch {
                                            val commitsResult = gitHubService.fetchRecentCommits(parsedRepo.first, parsedRepo.second, projectId)
                                            commitsResult.onSuccess { live ->
                                                commitFeed = live.data
                                                isLiveRepoData = live.isLiveData
                                                repoStatusNote = live.statusNote
                                            }
                                            isSyncing = false
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = brutal.accentOnSurface
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync",
                                    tint = brutal.accentOnSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (!isLiveRepoData) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BrutalBadge(
                                text = "CACHED PREVIEW",
                                containerColor = AmberGold,
                                contentColor = BrutalInkBlack,
                                borderColor = brutal.border
                            )
                            if (!repoStatusNote.isNullOrBlank()) {
                                Text(
                                    text = repoStatusNote ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = brutal.textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        RepoStatPill(
                            icon = Icons.Default.Star,
                            label = "Stars",
                            value = "${repoInfo?.stars ?: 42}",
                            accent = Color(0xFFFBBF24)
                        )
                        RepoStatPill(
                            icon = Icons.Default.ForkRight,
                            label = "Forks",
                            value = "${repoInfo?.forks ?: 8}",
                            accent = Color(0xFF38BDF8)
                        )
                        RepoStatPill(
                            icon = Icons.Default.WarningAmber,
                            label = "Issues",
                            value = "${repoInfo?.openIssues ?: 3}",
                            accent = Color(0xFFF87171)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All", "COMMITS" to "Commits", "PRS" to "PRs", "ISSUES" to "Issues").forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = if (isSelected) ElectricMint else brutal.surfaceRaised,
                                borderColor = if (isSelected) BrutalShadowBlack else brutal.border,
                                borderWidth = 1.5.dp,
                                shadowColor = if (isSelected) PrimaryIndigo else brutal.shadow,
                                shadowOffsetX = 2.dp,
                                shadowOffsetY = 2.dp,
                                onClick = { selectedFilter = key }
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) BrutalInkBlack else brutal.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Commit / Contribution List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    GithubCommitCard(
                        contribution = item,
                        onOpen = {
                            if (item.url.isNotBlank()) {
                                try {
                                    uriHandler.openUri(item.url)
                                } catch (ignored: Exception) {}
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RepoStatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .brutalCard(
                cornerRadius = 10.dp,
                containerColor = brutal.surfaceRaised,
                borderColor = brutal.border,
                borderWidth = 1.5.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 2.dp,
                shadowOffsetY = 2.dp
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = value, style = MaterialTheme.typography.labelLarge.copy(color = brutal.textPrimary, fontWeight = FontWeight.Black))
                Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = brutal.textMuted, fontSize = 9.sp))
            }
        }
    }
}

@Composable
fun GithubCommitCard(
    contribution: GitHubContributionDto,
    onOpen: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    val isPr = contribution.type.contains("PULL_REQUEST")
    val isIssue = contribution.type.contains("ISSUE")

    val icon = when {
        isPr -> Icons.AutoMirrored.Filled.MergeType
        isIssue -> Icons.Default.ReportProblem
        else -> Icons.Default.Commit
    }

    val typeColor = when {
        isPr -> Color(0xFFA855F7)
        isIssue -> Color(0xFFF59E0B)
        else -> ElectricMint
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tactileBrutalCard(
                cornerRadius = 14.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = typeColor,
                shadowOffsetX = 3.dp,
                shadowOffsetY = 3.dp,
                onClick = onOpen
            )
            .padding(14.dp)
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
                        containerColor = typeColor,
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
                    tint = BrutalInkBlack,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contribution.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = brutal.textPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(brutal.surfaceRaised)
                            .border(1.dp, brutal.border.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = contribution.githubEventId.ifEmpty { contribution.id }.take(7),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = brutal.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = contribution.contributionDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = brutal.textMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open in browser",
                tint = brutal.textPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
