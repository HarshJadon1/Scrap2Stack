package com.scrap2stack.app.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Tag
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
import androidx.compose.ui.window.Dialog
import com.scrap2stack.app.core.ui.components.*
import com.scrap2stack.app.domain.model.ChatMessage
import com.scrap2stack.app.feature.chat.components.CodeBlockBubble
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.PrimaryIndigoDark
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import com.scrap2stack.app.ui.theme.RevivalEmerald

@Composable
fun ChatScreen(
    projectId: String,
    viewModel: ChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }
    var showCodeDialog by remember { mutableStateOf(false) }
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
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        // Chat Header with Realtime Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Team Workspace Discussion",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ElectricMint)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Realtime Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ElectricMint
                    )
                }
            }
        }

        // Messages Feed
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = uiState) {
                is ChatUiState.Loading -> LoadingView()
                is ChatUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadMessages(projectId) }
                )
                is ChatUiState.Success -> {
                    if (state.messages.isEmpty()) {
                        EmptyStateView(
                            title = "No Team Messages Yet",
                            description = "Start the discussion! Share architecture notes, questions, or code snippets."
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(state.messages, key = { it.id.ifBlank { it.hashCode().toString() } }) { message ->
                                val isMe = message.senderId == state.currentUserId
                                EnhancedChatBubble(message = message, isMe = isMe)
                            }
                        }
                    }
                }
            }
        }

        // Quick Emoji Reaction Bar (Ergonomic 44dp touch targets)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val emojis = listOf("👍", "🚀", "🔥", "🎉", "💡", "👀", "✅")
            emojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .defaultMinSize(minWidth = 44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131D31))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(14.dp))
                        .bouncingClickable(scaleDown = 0.88f) {
                            viewModel.sendMessage(projectId, emoji)
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }
            }
        }

        // Glass Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.95f),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.04f))
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Code Snippet Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElectricMint.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, ElectricMint.copy(alpha = 0.3f)), CircleShape)
                        .bouncingClickable(scaleDown = 0.92f) { showCodeDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Share Code",
                        tint = ElectricMint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Type a message (or use ```code```)...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = false,
                    maxLines = 4
                )

                // Send Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (textInput.isNotBlank()) {
                                Brush.horizontalGradient(listOf(PrimaryIndigo, PrimaryIndigoDark))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
                            }
                        )
                        .bouncingClickable(
                            enabled = textInput.isNotBlank(),
                            scaleDown = 0.90f
                        ) {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(projectId, textInput)
                                textInput = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (textInput.isNotBlank()) Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Code Snippet Share Dialog
    if (showCodeDialog) {
        CodeSnippetModal(
            onDismiss = { showCodeDialog = false },
            onSendCode = { lang, codeText ->
                val formatted = "```$lang\n$codeText\n```"
                viewModel.sendMessage(projectId, formatted)
                showCodeDialog = false
            }
        )
    }
}

@Composable
fun EnhancedChatBubble(
    message: ChatMessage,
    isMe: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Avatar(
                name = message.senderName.ifBlank { "Team" },
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            if (!isMe) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = message.senderName.ifBlank { "Team Member" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = ElectricMint
                    )
                }
            }

            // Message Body (Splits plain text and ```code``` blocks)
            val bubbleBrush = if (isMe) {
                Brush.horizontalGradient(listOf(PrimaryIndigo, PrimaryIndigoDark))
            } else {
                Brush.linearGradient(listOf(Color(0xFF131D31), Color(0xFF0F172A)))
            }
            val bubbleBorder = if (isMe) {
                BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            } else {
                BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMe) 18.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 18.dp
                        )
                    )
                    .background(bubbleBrush)
                    .border(
                        bubbleBorder,
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMe) 18.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 18.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                ParsedMessageContent(rawText = message.message)
            }

            // Timestamp
            if (message.createdAt.isNotEmpty()) {
                val timeStr = message.createdAt.substringAfter("T").take(5).ifBlank { "" }
                if (timeStr.isNotEmpty()) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Parses message text to render normal paragraphs and embedded CodeBlockBubbles
 */
@Composable
fun ParsedMessageContent(rawText: String) {
    val codeBlockRegex = Regex("```(\\w+)?\\n([\\s\\S]*?)```")
    val match = codeBlockRegex.find(rawText)

    if (match != null) {
        val prefix = rawText.substring(0, match.range.first).trim()
        val language = match.groupValues[1].ifBlank { "code" }
        val codeSnippet = match.groupValues[2].trimEnd()
        val suffix = rawText.substring(match.range.last + 1).trim()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (prefix.isNotEmpty()) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = Color.White
                )
            }

            CodeBlockBubble(
                language = language,
                code = codeSnippet
            )

            if (suffix.isNotEmpty()) {
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = Color.White
                )
            }
        }
    } else {
        Text(
            text = rawText,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            color = Color.White
        )
    }
}

/**
 * Dialog to share a structured code snippet
 */
@Composable
fun CodeSnippetModal(
    onDismiss: () -> Unit,
    onSendCode: (String, String) -> Unit
) {
    var selectedLang by remember { mutableStateOf("kotlin") }
    var codeInput by remember { mutableStateOf("") }
    val languages = listOf("kotlin", "python", "javascript", "sql", "json", "dart")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Share Code Snippet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.6f))
                    }
                }

                // Language Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    languages.forEach { lang ->
                        val isSelected = selectedLang == lang
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricMint.copy(alpha = 0.15f) else Color(0xFF131D31),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ElectricMint.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.bouncingClickable(scaleDown = 0.94f) { selectedLang = lang }
                        ) {
                            Text(
                                text = lang.uppercase(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) ElectricMint else Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }

                // Code Input Area
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    placeholder = {
                        Text(
                            "Paste or write your $selectedLang code here...",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricMint,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = Color(0xFF090D16),
                        unfocusedContainerColor = Color(0xFF090D16)
                    )
                )

                Scrap2StackButton(
                    text = "Share with Team",
                    onClick = {
                        if (codeInput.isNotBlank()) {
                            onSendCode(selectedLang, codeInput)
                        }
                    },
                    enabled = codeInput.isNotBlank()
                )
            }
        }
    }
}
