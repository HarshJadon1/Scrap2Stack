package com.scrap2stack.app.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.ChatMessage

@Composable
fun ChatScreen(
    projectId: String,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(projectId) {
        viewModel.loadMessages(projectId)
        viewModel.subscribeToRealtimeChat(projectId)
    }

    LaunchedEffect(uiState) {
        if (uiState is ChatUiState.Success) {
            val messages = (uiState as ChatUiState.Success).messages
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader(title = "Team Discussion", modifier = Modifier.padding(horizontal = 8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = uiState) {
                is ChatUiState.Loading -> LoadingView()
                is ChatUiState.Error -> ErrorView(message = state.message, onRetry = { viewModel.loadMessages(projectId) })
                is ChatUiState.Success -> {
                    if (state.messages.isEmpty()) {
                        EmptyStateView(
                            title = "No team messages yet",
                            description = "Start the conversation with your project collaborators!"
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(state.messages) { message ->
                                val isMe = message.senderId == state.currentUserId
                                ChatBubble(message = message, isMe = isMe)
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Type a message...", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = false,
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendMessage(projectId, textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isMe: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Avatar(name = message.senderName.ifBlank { "Team Member" }, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            if (!isMe) {
                Text(
                    text = message.senderName.ifBlank { "Team Member" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}
