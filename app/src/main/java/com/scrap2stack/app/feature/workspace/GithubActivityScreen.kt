package com.scrap2stack.app.feature.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.Scrap2StackCard
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.data.remote.dto.GitHubContributionDto

@Composable
fun GithubActivityScreen(
    projectId: String,
    contributions: List<GitHubContributionDto> = emptyList(),
    onSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "GitHub Activity")
            IconButton(onClick = onSync) {
                Icon(Icons.Default.Refresh, contentDescription = "Sync GitHub")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActivityStat("Commits", contributions.count { it.type == "COMMIT" }.toString())
            ActivityStat("PRs", contributions.count { it.type.contains("PULL_REQUEST") }.toString())
            ActivityStat("Issues", contributions.count { it.type.contains("ISSUE") }.toString())
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (contributions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No activity detected yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(contributions.sortedByDescending { it.contributionDate }) { contribution ->
                    GithubActivityItem(contribution)
                }
            }
        }
    }
}

@Composable
fun GithubActivityItem(contribution: GitHubContributionDto) {
    Scrap2StackCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            val icon = when (contribution.type) {
                "COMMIT" -> Icons.Default.Commit
                "PULL_REQUEST", "MERGED_PULL_REQUEST" -> Icons.AutoMirrored.Filled.MergeType
                "ISSUE", "ISSUE_CLOSED" -> Icons.Default.ReportProblem
                "RELEASE" -> Icons.Default.NewReleases
                else -> Icons.Default.Code
            }
            
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = contribution.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${contribution.type} • ${contribution.contributionDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (contribution.verified) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.CheckCircle, 
                            contentDescription = "Verified", 
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
