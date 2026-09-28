package com.scrap2stack.app.feature.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrap2stack.app.core.ui.components.bouncingClickable
import com.scrap2stack.app.ui.theme.ElectricMint
import com.scrap2stack.app.ui.theme.NeonCyan
import com.scrap2stack.app.ui.theme.PrimaryIndigoLight
import kotlinx.coroutines.delay

@Composable
fun CodeBlockBubble(
    language: String,
    code: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    val displayLang = language.ifBlank { "CODE" }.uppercase()
    val highlightedCode = remember(code, displayLang) {
        formatSyntaxHighlighting(code, displayLang)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF090D16))
            .border(
                BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF101726))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricMint.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ElectricMint.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = displayLang,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = ElectricMint,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Copy Action Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .bouncingClickable(scaleDown = 0.90f) {
                            clipboardManager.setText(AnnotatedString(code))
                            isCopied = true
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        modifier = Modifier.size(13.dp),
                        tint = if (isCopied) ElectricMint else Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isCopied) ElectricMint else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = highlightedCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Basic multi-language syntax highlighter returning styled AnnotatedString
 */
private fun formatSyntaxHighlighting(code: String, language: String): AnnotatedString {
    val keywords = setOf(
        "fun", "val", "var", "class", "interface", "object", "suspend", "override",
        "import", "package", "return", "if", "else", "when", "for", "while", "true", "false",
        "null", "def", "from", "async", "await", "function", "const", "let", "export",
        "SELECT", "FROM", "WHERE", "JOIN", "INSERT", "UPDATE", "DELETE", "GROUP", "ORDER",
        "BY", "CREATE", "TABLE", "type"
    )

    return buildAnnotatedString {
        val lines = code.lines()
        lines.forEachIndexed { lineIdx, line ->
            var i = 0
            val trimmed = line.trimStart()
            if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("--")) {
                // Entire comment line
                append(AnnotatedString(line, SpanStyle(color = Color(0xFF64748B))))
            } else {
                val tokens = line.split(Regex("(?<=\\b|\\s|[.,();{}\\[\\]])|(?=\\b|\\s|[.,();{}\\[\\]])"))
                for (token in tokens) {
                    when {
                        token in keywords -> {
                            append(AnnotatedString(token, SpanStyle(color = PrimaryIndigoLight, fontWeight = FontWeight.Bold)))
                        }
                        token.startsWith("\"") || token.endsWith("\"") || token.startsWith("'") || token.endsWith("'") -> {
                            append(AnnotatedString(token, SpanStyle(color = Color(0xFFFDE047)))) // String yellow
                        }
                        token.toIntOrNull() != null -> {
                            append(AnnotatedString(token, SpanStyle(color = NeonCyan))) // Number cyan
                        }
                        token.firstOrNull()?.isUpperCase() == true && token.all { it.isLetterOrDigit() } -> {
                            append(AnnotatedString(token, SpanStyle(color = ElectricMint, fontWeight = FontWeight.SemiBold))) // Type/Class
                        }
                        else -> {
                            append(AnnotatedString(token, SpanStyle(color = Color(0xFFE2E8F0)))) // Default code white
                        }
                    }
                }
            }
            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}
