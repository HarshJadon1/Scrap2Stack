package com.scrap2stack.app.feature.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.Avatar
import com.scrap2stack.app.core.ui.components.EmptyStateView
import com.scrap2stack.app.core.ui.components.ErrorView
import com.scrap2stack.app.core.ui.components.LoadingView
import com.scrap2stack.app.core.ui.components.Scrap2StackCard
import com.scrap2stack.app.core.ui.components.SectionHeader
import com.scrap2stack.app.core.ui.components.dotGridBackground
import com.scrap2stack.app.core.ui.components.tactileBrutalCard
import com.scrap2stack.app.domain.model.ProjectMember
import com.scrap2stack.app.domain.model.ProjectRole
import com.scrap2stack.app.ui.theme.AmberGold
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    projectId: String,
    viewModel: TeamViewModel,
    onNavigateBack: (() -> Unit)?,
    onInviteDeveloper: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val brutal = MaterialTheme.brutalColors

    LaunchedEffect(projectId) {
        viewModel.loadTeam(projectId)
    }

    Scaffold(
        containerColor = brutal.canvas,
        topBar = {
            if (onNavigateBack != null) {
                TopAppBar(
                    title = { Text("Project Team", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onInviteDeveloper) {
                            Icon(Icons.Default.Add, contentDescription = "Invite")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = brutal.canvas)
                )
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
                is TeamUiState.Loading -> LoadingView()
                is TeamUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadTeam(projectId) })
                is TeamUiState.Success -> {
                    if (state.members.isEmpty()) {
                        EmptyTeamView()
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 28.dp)
                        ) {
                            if (onNavigateBack == null) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SectionHeader(title = "Project Team", modifier = Modifier.weight(1f))
                                        Box(
                                            modifier = Modifier
                                                .tactileBrutalCard(
                                                    cornerRadius = 10.dp,
                                                    containerColor = ElectricMint,
                                                    borderColor = BrutalShadowBlack,
                                                    borderWidth = 2.dp,
                                                    shadowColor = BrutalShadowBlack,
                                                    shadowOffsetX = 3.dp,
                                                    shadowOffsetY = 3.dp,
                                                    onClick = onInviteDeveloper
                                                )
                                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = BrutalInkBlack, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "INVITE",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = BrutalInkBlack
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                            }
                            items(state.members) { member ->
                                TeamMemberCard(member)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMemberCard(member: ProjectMember) {
    val brutal = MaterialTheme.brutalColors
    Scrap2StackCard(shadowColor = PrimaryIndigo) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(name = member.user?.name ?: "User", modifier = Modifier.size(46.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.user?.name ?: "Unknown Developer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = brutal.textPrimary
                )
                Text(
                    text = member.role.name.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = brutal.accentOnSurface
                )
            }
            if (member.role == ProjectRole.OWNER) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AmberGold)
                        .border(1.5.dp, BrutalShadowBlack, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LEAD",
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
}

@Composable
fun EmptyTeamView() {
    EmptyStateView(
        title = "No Team Members Yet",
        description = "Invite developers from Synergy Matches to collaborate on this revival."
    )
}
