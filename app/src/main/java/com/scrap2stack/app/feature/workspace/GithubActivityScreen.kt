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
import com.scrap2stack.app.core.ui.components.bouncingClickable
import com.scrap2stack.app.core.ui.components.glassSurface
import com.scrap2stack.app.data.remote.dto.GitHubContributionDto
import com.scrap2stack.app.domain.model.GitHubRepoInfo
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.GlassWhiteBorder
import kotlinx.coroutines.launch

@Composable
fun GithubActivityScreen(
    projectId: String,
    repoUrl: String = "https://github.com/Scrap2Stack/revival-engine",
    contributions: List<GitHubContributionDto> = emptyList(),
    onSync: () -> Unit = {}
) {
    val gitHubService = remember { GitHubService() }
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var repoInfo by remember { mutableStateOf<GitHubRepoInfo?>(null) }
    var commitFeed by remember { mutableStateOf<List<GitHubContributionDto>>(contributions) }
    var isSyncing by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val parsedRepo = remember(repoUrl) {
        gitHubService.parseRepoUrl(repoUrl) ?: Pair("Scrap2Stack", "revival-engine")
    }

    LaunchedEffect(projectId, repoUrl) {
        isSyncing = true
        coroutineScope.launch {
            val infoResult = gitHubService.fetchRepoInfo(parsedRepo.first, parsedRepo.second)
            infoResult.onSuccess { repoInfo = it }

            val commitsResult = gitHubService.fetchRecentCommits(parsedRepo.first, parsedRepo.second, projectId)
            commitsResult.onSuccess { commitFeed = it }
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
            .background(Color(0xFF080B11))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Repo Overview Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassSurface(shape = RoundedCornerShape(22.dp))
                    .padding(18.dp)
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
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = ElectricMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${parsedRepo.first}/${parsedRepo.second}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Branch: ${repoInfo?.defaultBranch ?: "master"} • ${repoInfo?.primaryLanguage ?: "Kotlin"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectricMint,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        // Live Sync Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricMint.copy(alpha = 0.15f))
                                .border(1.dp, ElectricMint.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .bouncingClickable(enabled = !isSyncing) {
                                isSyncing = true
                                onSync()
                                coroutineScope.launch {
                                    val commitsResult = gitHubService.fetchRecentCommits(parsedRepo.first, parsedRepo.second, projectId)
                                    commitsResult.onSuccess { commitFeed = it }
                                    isSyncing = false
                                }
                            }
                            .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = ElectricMint
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync",
                                    tint = ElectricMint,
                                    modifier = Modifier.size(18.dp)
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricMint.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                            .border(1.dp, if (isSelected) ElectricMint else GlassWhiteBorder, RoundedCornerShape(12.dp))
                            .bouncingClickable { selectedFilter = key }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) ElectricMint else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.5.dp, GlassWhiteBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = value, style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
                Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 10.sp))
            }
        }
    }
}

@Composable
fun GithubCommitCard(
    contribution: GitHubContributionDto,
    onOpen: () -> Unit
) {
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
            .glassSurface(shape = RoundedCornerShape(16.dp))
            .bouncingClickable { onOpen() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contribution.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // SHA tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = contribution.githubEventId.ifEmpty { contribution.id }.take(7),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = contribution.contributionDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open in browser",
                tint = Color(0xFF475569),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
