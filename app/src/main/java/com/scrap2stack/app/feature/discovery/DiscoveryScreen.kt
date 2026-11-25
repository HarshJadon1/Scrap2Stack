package com.scrap2stack.app.feature.discovery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

enum class SortOrder {
    REVIVAL_DESC, REVIVAL_ASC, NEWEST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToProjectDetails: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var sortOrder by remember { mutableStateOf(SortOrder.REVIVAL_DESC) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val brutal = MaterialTheme.brutalColors

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXPLORE //",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 11.sp
                            ),
                            color = brutal.accentOnSurface
                        )
                        Text(
                            text = "Discover Projects",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = brutal.textPrimary
                        )
                    }

                    // Tactile Filter Keycap Button
                    val filterActive = selectedCategory != "ALL" || selectedStatus != "ALL"
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .tactileBrutalCard(
                                cornerRadius = 12.dp,
                                containerColor = if (filterActive) ElectricMint else brutal.surfaceRaised,
                                borderColor = if (filterActive) BrutalShadowBlack else brutal.border,
                                borderWidth = 2.dp,
                                shadowColor = if (filterActive) PrimaryIndigo else brutal.shadow,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = { showFilterSheet = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            modifier = Modifier.size(20.dp),
                            tint = if (filterActive) BrutalInkBlack else brutal.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Neo-Brutalist Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .brutalCard(
                            cornerRadius = 14.dp,
                            containerColor = brutal.surface,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = brutal.shadow,
                            shadowOffsetX = 4.dp,
                            shadowOffsetY = 4.dp
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = brutal.accentOnSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    "Search tech, skills, or projects...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = brutal.textMuted
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = brutal.textPrimary,
                                unfocusedTextColor = brutal.textPrimary
                            )
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = brutal.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Filter Category Sticker Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("ALL", "🚀 Shipped", "Web", "Mobile", "AI/ML", "Open Source", "DevOps")
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .tactileBrutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = if (isSelected) ElectricMint else brutal.surfaceRaised,
                                    borderColor = if (isSelected) BrutalShadowBlack else brutal.border,
                                    borderWidth = 1.5.dp,
                                    shadowColor = if (isSelected) PrimaryIndigo else brutal.shadow,
                                    shadowOffsetX = 3.dp,
                                    shadowOffsetY = 3.dp,
                                    onClick = { selectedCategory = category }
                                )
                                .padding(horizontal = 13.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) BrutalInkBlack else brutal.textPrimary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .dotGridBackground()
        ) {
            when (val state = uiState) {
                is DiscoveryUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        repeat(4) {
                            ProjectCardSkeleton()
                        }
                    }
                }
                is DiscoveryUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadProjects() }
                )
                is DiscoveryUiState.Empty -> {
                    EmptyStateView(
                        title = "No Projects Discovered",
                        description = "There are no projects available in the catalog yet."
                    )
                }
                is DiscoveryUiState.Success -> {
                    var projects = state.projects.filter { project ->
                        val matchesQuery = searchQuery.isEmpty() ||
                            project.name.contains(searchQuery, ignoreCase = true) ||
                            project.description.contains(searchQuery, ignoreCase = true) ||
                            project.technologies.any { tech -> tech.contains(searchQuery, ignoreCase = true) } ||
                            project.requiredSkills.any { skill -> skill.contains(searchQuery, ignoreCase = true) }

                        val matchesCategory = when (selectedCategory) {
                            "ALL" -> true
                            "🚀 Shipped" -> project.status.name.equals("COMPLETED", ignoreCase = true)
                            else -> project.category.equals(selectedCategory, ignoreCase = true)
                        }
                        val matchesStatus = selectedStatus == "ALL" || project.status.name.equals(selectedStatus, ignoreCase = true)

                        matchesQuery && matchesCategory && matchesStatus
                    }

                    projects = when (sortOrder) {
                        SortOrder.REVIVAL_DESC -> projects.sortedByDescending { it.revivalScore }
                        SortOrder.REVIVAL_ASC -> projects.sortedBy { it.revivalScore }
                        SortOrder.NEWEST -> projects
                    }

                    if (projects.isEmpty()) {
                        EmptyStateView(
                            title = "No Matches Found",
                            description = if (searchQuery.isNotEmpty() || selectedCategory != "ALL" || selectedStatus != "ALL") 
                                "Try adjusting your search keywords or active filters." 
                            else 
                                "No projects match your current filters."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 28.dp)
                        ) {
                            items(projects, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    onClick = { onNavigateToProjectDetails(project.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = brutal.surface,
                scrimColor = Color.Black.copy(alpha = 0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Filter & Sort Projects",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = brutal.textPrimary
                    )

                    Text(
                        text = "Sort By Revival Score",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = brutal.accentOnSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = sortOrder == SortOrder.REVIVAL_DESC,
                            onClick = { sortOrder = SortOrder.REVIVAL_DESC },
                            label = { Text("Highest First") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricMint.copy(alpha = 0.15f),
                                selectedLabelColor = ElectricMint
                            )
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.REVIVAL_ASC,
                            onClick = { sortOrder = SortOrder.REVIVAL_ASC },
                            label = { Text("Lowest First") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricMint.copy(alpha = 0.15f),
                                selectedLabelColor = ElectricMint
                            )
                        )
                    }

                    Text(
                        text = "Status Filter",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ElectricMint
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "ABANDONED", "INCOMPLETE", "REVIVING", "COMPLETED").forEach { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                label = { Text(status) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricMint.copy(alpha = 0.15f),
                                    selectedLabelColor = ElectricMint
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Scrap2StackButton(
                        text = "Apply Filters",
                        onClick = { showFilterSheet = false }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
