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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.EmptyStateView
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.domain.model.CharmContribution
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.RevivalEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharmsScreen(
    viewModel: CharmsViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            if (onNavigateBack != null) {
                TopAppBar(
                    title = {
                        Text(
                            "Reputation Hub",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                                Text(
                                    text = "Reputation Hub",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Charms measure your trust, mentorship, and code revival footprint.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        // Hero Gamified Charms Card
                        item {
                            CharmsHeroCard(totalCharms = state.totalCharms)
                        }

                        // History Section Header
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryIndigo.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = PrimaryIndigoLight
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Contribution History",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = Color.White
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
    val cardBrush = Brush.verticalGradient(
        listOf(Color(0xFF131D33), Color(0xFF0F172A))
    )
    val borderBrush = Brush.linearGradient(
        listOf(
            ElectricMint.copy(alpha = 0.45f),
            PrimaryIndigo.copy(alpha = 0.25f),
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(cardBrush)
            .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(26.dp))
            .padding(26.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ElectricMint.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .border(BorderStroke(1.5.dp, ElectricMint.copy(alpha = 0.5f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = ElectricMint
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "$totalCharms",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 46.sp,
                    letterSpacing = (-1).sp
                ),
                color = ElectricMint
            )

            Text(
                text = "TOTAL REPUTATION CHARMS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    fontSize = 11.sp
                ),
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.04f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = ElectricMint
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Level 2: Priority Collaboration Requests Active",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun CharmHistoryRow(item: CharmContribution) {
    val rowBorder = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.09f), Color.Transparent)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF10192A))
            .border(BorderStroke(1.dp, rowBorder), RoundedCornerShape(18.dp))
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
                if (!item.projectName.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Project: ${item.projectName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = ElectricMint.copy(alpha = 0.85f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricMint.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ElectricMint.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "+${item.charms} Charms",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        ),
                        color = ElectricMint
                    )
                }
                if (item.createdAt.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.createdAt.take(10),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
