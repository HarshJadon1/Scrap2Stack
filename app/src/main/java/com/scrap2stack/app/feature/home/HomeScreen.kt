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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.Developer
import com.scrap2stack.app.domain.model.Project
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.NeonCyan
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoDark
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.RevivalEmerald

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProjectDetails: (String) -> Unit,
    onNavigateToCharms: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    Scaffold(
        topBar = {
            val user = (uiState as? HomeUiState.Success)?.user
            HomeHeader(
                user = user,
                onNavigateToCharms = onNavigateToCharms,
                onRefresh = { viewModel.loadHomeData() }
            )
        }
    ) { innerPadding ->
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
                            .clip(RoundedCornerShape(24.dp))
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
    val passBrush = Brush.linearGradient(
        listOf(
            Color(0xFF131D33),
            Color(0xFF0F172A),
            Color(0xFF0A0F1D)
        )
    )
    val borderBrush = Brush.linearGradient(
        listOf(
            ElectricMint.copy(alpha = 0.5f),
            PrimaryIndigo.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.08f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(passBrush)
            .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(24.dp))
            .bouncingClickable(scaleDown = 0.98f, onClick = onClick)
            .padding(22.dp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ElectricMint, RevivalEmerald))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color(0xFF080B11)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DEVELOPER TIER 2",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 10.sp
                            ),
                            color = ElectricMint
                        )
                        Text(
                            text = "Code Alchemist",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Reputation Charms Counter Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElectricMint.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ElectricMint.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = ElectricMint
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$charms",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            ),
                            color = ElectricMint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tier Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next Tier: Stack Master",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "$charms / 200 Charms",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                val progress = (charms / 200f).coerceIn(0.05f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(PrimaryIndigo, ElectricMint)
                            )
                        )
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
    val highlightBorder = Brush.linearGradient(
        listOf(
            ElectricMint.copy(alpha = 0.45f),
            PrimaryIndigo.copy(alpha = 0.35f),
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF131D33), Color(0xFF0F172A))
                )
            )
            .border(BorderStroke(1.dp, highlightBorder), RoundedCornerShape(22.dp))
            .bouncingClickable(scaleDown = 0.98f, onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryIndigo.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryIndigoLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Synergy Match",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = PrimaryIndigoLight
                        )
                    }
                }
                
                Surface(
                    color = ElectricMint.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElectricMint.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Revival: ${project.revivalScore}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = ElectricMint
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
    val cardBorder = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)
    )

    Box(
        modifier = Modifier
            .width(210.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131D31))
            .border(BorderStroke(1.dp, cardBorder), RoundedCornerShape(18.dp))
            .bouncingClickable(scaleDown = 0.96f, onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = project.technologies.take(2).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ElectricMint.copy(alpha = 0.8f),
                    maxLines = 1
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
                        color = ElectricMint
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Revival",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${user?.name?.ifBlank { "Developer" } ?: "Developer"} 👋",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = (-0.4).sp
                ),
                color = Color.White
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), CircleShape)
                    .bouncingClickable(scaleDown = 0.88f, onClick = onRefresh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Feeds",
                    tint = ElectricMint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Avatar(name = user?.name ?: "U", modifier = Modifier.size(46.dp))
        }
    }
}
