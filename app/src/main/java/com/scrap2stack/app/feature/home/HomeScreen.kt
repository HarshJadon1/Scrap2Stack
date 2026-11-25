package com.scrap2stack.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProjectDetails: (String) -> Unit,
    onNavigateToCharms: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val brutal = MaterialTheme.brutalColors

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            val user = (uiState as? HomeUiState.Success)?.user
            HomeHeader(
                user = user,
                onNavigateToCharms = onNavigateToCharms,
                onRefresh = { viewModel.loadHomeData() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dotGridBackground()
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .brutalCard(
                                    cornerRadius = 16.dp,
                                    containerColor = brutal.surface,
                                    borderColor = brutal.border,
                                    borderWidth = 2.dp,
                                    shadowColor = brutal.shadow
                                )
                                .shimmerEffect()
                        )
                        repeat(3) {
                            ProjectCardSkeleton()
                        }
                    }
                }
                is HomeUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.loadHomeData() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                is HomeUiState.Success -> {
                    HomeContent(
                        innerPadding = innerPadding,
                        user = state.user,
                        recommendedProjects = state.recommendedProjects,
                        trendingProjects = state.trendingProjects,
                        onNavigateToProjectDetails = onNavigateToProjectDetails,
                        onNavigateToCharms = onNavigateToCharms
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    innerPadding: PaddingValues,
    user: Developer?,
    recommendedProjects: List<Project>,
    trendingProjects: List<Project>,
    onNavigateToProjectDetails: (String) -> Unit,
    onNavigateToCharms: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Developer Pass Card
        item {
            DeveloperPassCard(
                charms = user?.charms ?: 0,
                developerName = user?.name?.ifBlank { "Developer" } ?: "Developer",
                onClick = onNavigateToCharms
            )
        }

        // Recommended Match Section
        if (recommendedProjects.isNotEmpty()) {
            item {
                SectionHeader(title = "Top Match For You")
            }

            items(recommendedProjects, key = { "rec_${it.id}" }) { project ->
                RecommendedProjectCard(
                    project = project,
                    onClick = { onNavigateToProjectDetails(project.id) }
                )
            }
        }

        // Revival Projects Section
        if (trendingProjects.isNotEmpty()) {
            item {
                SectionHeader(title = "Projects Ready to Revive")
            }

            items(trendingProjects, key = { "trend_${it.id}" }) { project ->
                ProjectCard(
                    project = project,
                    onClick = { onNavigateToProjectDetails(project.id) }
                )
            }

            item {
                SectionHeader(title = "High Revival Potential")
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(trendingProjects.sortedByDescending { it.revivalScore }, key = { "compact_${it.id}" }) { project ->
                        CompactProjectCard(
                            project = project,
                            onClick = { onNavigateToProjectDetails(project.id) }
                        )
                    }
                }
            }
        } else if (recommendedProjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No Projects Yet",
                        description = "Be the first to publish or revive a project in your tech stack!"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun DeveloperPassCard(
    charms: Int,
    developerName: String,
    onClick: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tactileBrutalCard(
                cornerRadius = 18.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = PrimaryIndigo,
                shadowOffsetX = 5.dp,
                shadowOffsetY = 5.dp,
                onClick = onClick
            )
            .padding(20.dp)
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
                            .size(40.dp)
                            .brutalCard(
                                cornerRadius = 10.dp,
                                containerColor = ElectricMint,
                                borderColor = BrutalShadowBlack,
                                borderWidth = 2.dp,
                                shadowColor = BrutalShadowBlack,
                                shadowOffsetX = 2.dp,
                                shadowOffsetY = 2.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = BrutalInkBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Box(
                            modifier = Modifier
                                .graphicsLayer { rotationZ = -2f }
                                .clip(RoundedCornerShape(5.dp))
                                .background(AmberGold)
                                .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TIER 2",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.6.sp,
                                    fontSize = 10.sp
                                ),
                                color = BrutalInkBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Code Alchemist",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            color = brutal.textPrimary
                        )
                    }
                }

                // Reputation Charms Counter Pill
                Box(
                    modifier = Modifier.brutalCard(
                        cornerRadius = 10.dp,
                        containerColor = ElectricMint,
                        borderColor = BrutalShadowBlack,
                        borderWidth = 2.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BrutalInkBlack
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$charms",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            ),
                            color = BrutalInkBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tier Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next Tier: Stack Master",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = brutal.textMuted
                )
                Text(
                    text = "$charms / 200 CHARMS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = brutal.accentOnSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(brutal.surfaceRaised)
                    .border(1.5.dp, brutal.border, RoundedCornerShape(5.dp))
            ) {
                val progress = (charms / 200f).coerceIn(0.05f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(ElectricMint)
                )
            }
        }
    }
}

@Composable
fun RecommendedProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .tactileBrutalCard(
                cornerRadius = 18.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = ElectricMint,
                shadowOffsetX = 5.dp,
                shadowOffsetY = 5.dp,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.brutalCard(
                        cornerRadius = 8.dp,
                        containerColor = PrimaryIndigo,
                        borderColor = BrutalShadowBlack,
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
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "SYNERGY MATCH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier.brutalCard(
                        cornerRadius = 8.dp,
                        containerColor = ElectricMint,
                        borderColor = BrutalShadowBlack,
                        borderWidth = 1.5.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 2.dp,
                        shadowOffsetY = 2.dp
                    )
                ) {
                    Text(
                        text = "SRVM ${project.revivalScore}%",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        ),
                        color = BrutalInkBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = (-0.3).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = brutal.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                color = brutal.textPrimary.copy(alpha = 0.80f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                project.requiredSkills.take(4).forEach { skill ->
                    SkillChip(skill = skill)
                }
            }
        }
    }
}

@Composable
fun CompactProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    val brutal = MaterialTheme.brutalColors
    Box(
        modifier = Modifier
            .width(214.dp)
            .height(136.dp)
            .tactileBrutalCard(
                cornerRadius = 16.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = PrimaryIndigo,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = brutal.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = project.technologies.take(2).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = brutal.accentOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${project.revivalScore}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        ),
                        color = brutal.accentOnSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REVIVAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = brutal.textMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = brutal.textPrimary
                )
            }
        }
    }
}

@Composable
fun HomeHeader(
    user: Developer?,
    onNavigateToCharms: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    val brutal = MaterialTheme.brutalColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "WELCOME BACK //",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontSize = 11.sp
                ),
                color = brutal.accentOnSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${user?.name?.ifBlank { "Developer" } ?: "Developer"} 👋",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = (-0.4).sp
                ),
                color = brutal.textPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .tactileBrutalCard(
                        cornerRadius = 12.dp,
                        containerColor = brutal.surfaceRaised,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = ElectricMint,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp,
                        onClick = onRefresh
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Feeds",
                    tint = brutal.accentOnSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Avatar(name = user?.name ?: "U", modifier = Modifier.size(46.dp))
        }
    }
}
