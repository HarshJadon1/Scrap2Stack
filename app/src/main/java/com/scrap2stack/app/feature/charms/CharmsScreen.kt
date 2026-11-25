package com.scrap2stack.app.feature.charms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.BrutalBadge
import com.scrap2stack.app.core.ui.components.BrutalHeaderTag
import com.scrap2stack.app.core.ui.components.EmptyStateView
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.brutalCard
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharmsScreen(
    viewModel: CharmsViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val brutal = MaterialTheme.brutalColors
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            if (onNavigateBack != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brutal.canvas)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = brutal.surfaceRaised,
                                borderColor = brutal.border,
                                borderWidth = 2.dp,
                                shadowColor = brutal.shadow,
                                shadowOffsetX = 3.dp,
                                shadowOffsetY = 3.dp,
                                onClick = onNavigateBack
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = brutal.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        BrutalHeaderTag(text = "REP // CHARMS")
                        Text(
                            "Reputation Hub",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = brutal.textPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .dotGridBackground()
        ) {
            when (val state = uiState) {
                is CharmsUiState.Loading -> LoadingView(modifier = Modifier.padding(20.dp))
                is CharmsUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadCharmsData() }
                )
                is CharmsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            if (onNavigateBack == null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                BrutalHeaderTag(text = "REP // CHARMS")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reputation Hub",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = brutal.textPrimary
                                )
                            }
                            Text(
                                text = "Charms measure your trust, mentorship, and code revival footprint.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = brutal.textMuted
                            )
                        }

                        // Hero Gamified Charms Card
                        item {
                            CharmsHeroCard(totalCharms = state.totalCharms)
                        }

                        // History Section Header
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .brutalCard(
                                            cornerRadius = 8.dp,
                                            containerColor = PrimaryIndigo,
                                            borderColor = brutal.border,
                                            borderWidth = 1.5.dp,
                                            shadowColor = brutal.shadow,
                                            shadowOffsetX = 2.dp,
                                            shadowOffsetY = 2.dp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Contribution History",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    ),
                                    color = brutal.textPrimary
                                )
                            }
                        }

                        if (state.history.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No Charms Recorded Yet",
                                    description = "Accept collaboration requests and ship project milestones to earn Charms!"
                                )
                            }
                        } else {
                            items(state.history) { item ->
                                CharmHistoryRow(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CharmsHeroCard(totalCharms: Int) {
    val brutal = MaterialTheme.brutalColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 18.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 5.dp,
                shadowOffsetY = 5.dp
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .brutalCard(
                        cornerRadius = 16.dp,
                        containerColor = ElectricMint,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = BrutalShadowBlack,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = BrutalInkBlack
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "$totalCharms",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 46.sp,
                    letterSpacing = (-1).sp
                ),
                color = brutal.accentOnSurface
            )

            Text(
                text = "TOTAL REPUTATION CHARMS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    fontSize = 11.sp
                ),
                color = brutal.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .brutalCard(
                        cornerRadius = 12.dp,
                        containerColor = brutal.surfaceRaised,
                        borderColor = brutal.border,
                        borderWidth = 1.5.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 2.dp,
                        shadowOffsetY = 2.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = brutal.accentOnSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Level 2: Priority Collaboration Requests Active",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = brutal.textPrimary
                )
            }
        }
    }
}

@Composable
fun CharmHistoryRow(item: CharmContribution) {
    val brutal = MaterialTheme.brutalColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .brutalCard(
                cornerRadius = 14.dp,
                containerColor = brutal.surface,
                borderColor = brutal.border,
                borderWidth = 2.dp,
                shadowColor = brutal.shadow,
                shadowOffsetX = 4.dp,
                shadowOffsetY = 4.dp
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description.ifEmpty { item.contributionType.name },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    color = brutal.textPrimary
                )
                if (!item.projectName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Project: ${item.projectName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = brutal.accentOnSurface
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                BrutalBadge(
                    text = "+${item.charms} CHARMS",
                    containerColor = ElectricMint,
                    contentColor = BrutalInkBlack
                )
                if (item.createdAt.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = brutal.textMuted
                    )
                }
            }
        }
    }
}
