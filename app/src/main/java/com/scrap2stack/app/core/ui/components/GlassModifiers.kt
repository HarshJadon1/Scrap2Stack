package com.scrap2stack.app.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.scrap2stack.app.ui.theme.GlassWhiteBorder
import com.scrap2stack.app.ui.theme.GlassWhiteBorderSubtle
import com.scrap2stack.app.ui.theme.ShimmerBaseDark
import com.scrap2stack.app.ui.theme.ShimmerHighlightDark

/**
 * Applies a modern glassmorphic surface with dual-tone gradient border
 */
fun Modifier.glassSurface(
    shape: Shape,
    backgroundColor: Color = Color(0xFF0F172A).copy(alpha = 0.82f),
    borderBrush: Brush = Brush.linearGradient(
        listOf(
            GlassWhiteBorder,
            GlassWhiteBorderSubtle,
            Color.Transparent
        )
    )
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .border(BorderStroke(1.dp, borderBrush), shape)

/**
 * Tactile bouncy click animation for interactive cards, pills and buttons
 */
fun Modifier.bouncingClickable(
    enabled: Boolean = true,
    scaleDown: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce_scale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Shimmer effect modifier for skeleton placeholder loading
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * (size.width.coerceAtLeast(1).toFloat()),
        targetValue = 2 * (size.width.coerceAtLeast(1).toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    this
        .onGloballyPositioned { size = it.size }
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    ShimmerBaseDark,
                    ShimmerHighlightDark,
                    ShimmerBaseDark
                ),
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.coerceAtLeast(1).toFloat(), size.height.coerceAtLeast(1).toFloat())
            )
        )
}
