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
import com.scrap2stack.app.ui.theme.BrutalInkBlack
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.PrimaryIndigo
import com.scrap2stack.app.ui.theme.brutalColors

@Composable
fun ChatScreen(
    projectId: String,
    viewModel: ChatViewModel
) {
    val brutal = MaterialTheme.brutalColors
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
            .background(brutal.canvas)
            .dotGridBackground()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        // Chat Header with Realtime Sticker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                BrutalHeaderTag(text = "TEAM // CHANNEL")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Team Workspace Discussion",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    ),
                    color = brutal.textPrimary
                )
            }
            BrutalBadge(
                text = "REALTIME",
                containerColor = ElectricMint,
                contentColor = BrutalInkBlack
            )
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

        // Quick Emoji Reaction Bar (Stack Pop Keycaps)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val emojis = listOf("👍", "🚀", "🔥", "🎉", "💡", "👀", "✅")
            emojis.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .defaultMinSize(minWidth = 44.dp)
                        .tactileBrutalCard(
                            cornerRadius = 12.dp,
                            containerColor = brutal.surface,
                            borderColor = brutal.border,
                            borderWidth = 1.5.dp,
                            shadowColor = brutal.shadow,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp,
                            onClick = { viewModel.sendMessage(projectId, emoji) }
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }
            }
        }

        // Stack Pop Input Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = brutal.shadow,
                    shadowOffsetX = 4.dp,
                    shadowOffsetY = 4.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Code Snippet Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .tactileBrutalCard(
                            cornerRadius = 11.dp,
                            containerColor = ElectricMint,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp,
                            onClick = { showCodeDialog = true }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Share Code",
                        tint = BrutalInkBlack,
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
                            color = brutal.textMuted
                        )
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = brutal.textPrimary),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = brutal.textPrimary,
                        unfocusedTextColor = brutal.textPrimary
                    ),
                    singleLine = false,
                    maxLines = 4
                )

                // Send Button
                val canSend = textInput.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .tactileBrutalCard(
                            cornerRadius = 11.dp,
                            containerColor = if (canSend) PrimaryIndigo else brutal.surfaceRaised,
                            borderColor = brutal.border,
                            borderWidth = 2.dp,
                            shadowColor = BrutalShadowBlack,
                            shadowOffsetX = 2.dp,
                            shadowOffsetY = 2.dp,
                            enabled = canSend,
                            onClick = {
                                if (canSend) {
                                    viewModel.sendMessage(projectId, textInput)
                                    textInput = ""
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) Color.White else brutal.textMuted,
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
    val brutal = MaterialTheme.brutalColors
    val bubbleContainer = if (isMe) PrimaryIndigo else brutal.surface
    val textColor = if (isMe) Color.White else brutal.textPrimary

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
                    modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = message.senderName.ifBlank { "Team Member" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = brutal.accentOnSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .brutalCard(
                        cornerRadius = 16.dp,
                        containerColor = bubbleContainer,
                        borderColor = brutal.border,
                        borderWidth = 2.dp,
                        shadowColor = brutal.shadow,
                        shadowOffsetX = 3.dp,
                        shadowOffsetY = 3.dp
                    )
                    .padding(14.dp)
            ) {
                ParsedMessageContent(rawText = message.message, textColor = textColor)
            }

            // Timestamp
            if (message.createdAt.isNotEmpty()) {
                val timeStr = message.createdAt.substringAfter("T").take(5).ifBlank { "" }
                if (timeStr.isNotEmpty()) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = brutal.textMuted,
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
fun ParsedMessageContent(
    rawText: String,
    textColor: Color = Color.White
) {
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
                    color = textColor
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
                    color = textColor
                )
            }
        }
    } else {
        Text(
            text = rawText,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            color = textColor
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
    val brutal = MaterialTheme.brutalColors
    var selectedLang by remember { mutableStateOf("kotlin") }
    var codeInput by remember { mutableStateOf("") }
    val languages = listOf("kotlin", "python", "javascript", "sql", "json", "dart")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .brutalCard(
                    cornerRadius = 18.dp,
                    containerColor = brutal.surface,
                    borderColor = brutal.border,
                    borderWidth = 2.dp,
                    shadowColor = BrutalShadowBlack,
                    shadowOffsetX = 5.dp,
                    shadowOffsetY = 5.dp
                )
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
                    Column {
                        BrutalHeaderTag(text = "SNIPPET // CODE")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Share Code Snippet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            ),
                            color = brutal.textPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .tactileBrutalCard(
                                cornerRadius = 10.dp,
                                containerColor = brutal.surfaceRaised,
                                borderColor = brutal.border,
                                borderWidth = 1.5.dp,
                                shadowColor = brutal.shadow,
                                shadowOffsetX = 2.dp,
                                shadowOffsetY = 2.dp,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = brutal.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Language Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        val isSelected = selectedLang == lang
                        Box(
                            modifier = Modifier
                                .tactileBrutalCard(
                                    cornerRadius = 10.dp,
                                    containerColor = if (isSelected) ElectricMint else brutal.surfaceRaised,
                                    borderColor = brutal.border,
                                    borderWidth = if (isSelected) 2.dp else 1.5.dp,
                                    shadowColor = brutal.shadow,
                                    shadowOffsetX = 2.dp,
                                    shadowOffsetY = 2.dp,
                                    onClick = { selectedLang = lang }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = lang.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (isSelected) BrutalInkBlack else brutal.textMuted
                            )
                        }
                    }
                }

                // Code Input Area (intentional dark terminal block)
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    placeholder = {
                        Text(
                            "Paste or write your $selectedLang code here...",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color.White.copy(alpha = 0.45f)
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
                        unfocusedBorderColor = brutal.border,
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
