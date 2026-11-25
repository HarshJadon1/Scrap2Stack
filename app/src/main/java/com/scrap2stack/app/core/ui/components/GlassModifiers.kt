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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.scrap2stack.app.ui.theme.BrutalBorderWhite
import com.scrap2stack.app.ui.theme.BrutalShadowBlack
import com.scrap2stack.app.ui.theme.BrutalSurfaceDark
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

/**
 * Draws a zero-blur solid Neo-Brutalist offset drop shadow behind the element.
 */
fun Modifier.brutalShadow(
    cornerRadius: Dp = 16.dp,
    shadowColor: Color = BrutalShadowBlack,
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp
): Modifier = this.drawBehind {
    val radiusPx = cornerRadius.toPx()
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = size,
        cornerRadius = CornerRadius(radiusPx, radiusPx)
    )
}

/**
 * Applies a crisp Neo-Brutalist surface with hard 2dp border and solid offset shadow.
 */
fun Modifier.brutalCard(
    cornerRadius: Dp = 16.dp,
    containerColor: Color = BrutalSurfaceDark,
    borderColor: Color = BrutalBorderWhite,
    borderWidth: Dp = 2.dp,
    shadowColor: Color = BrutalShadowBlack,
    shadowOffsetX: Dp = 4.dp,
    shadowOffsetY: Dp = 4.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .padding(end = shadowOffsetX, bottom = shadowOffsetY)
        .brutalShadow(
            cornerRadius = cornerRadius,
            shadowColor = shadowColor,
            offsetX = shadowOffsetX,
            offsetY = shadowOffsetY
        )
        .clip(shape)
        .background(containerColor, shape)
        .border(BorderStroke(borderWidth, borderColor), shape)
}

/**
 * Interactive Neo-Brutalist surface that physically depresses toward its solid offset shadow on press.
 */
fun Modifier.tactileBrutalCard(
    cornerRadius: Dp = 16.dp,
    containerColor: Color = BrutalSurfaceDark,
    borderColor: Color = BrutalBorderWhite,
    borderWidth: Dp = 2.dp,
    shadowColor: Color = BrutalShadowBlack,
    shadowOffsetX: Dp = 4.dp,
    shadowOffsetY: Dp = 4.dp,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressFraction by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "brutal_press"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val travelX = (shadowOffsetX - 1.dp).coerceAtLeast(1.dp)
    val travelY = (shadowOffsetY - 1.dp).coerceAtLeast(1.dp)

    this
        .padding(end = shadowOffsetX, bottom = shadowOffsetY)
        .brutalShadow(
            cornerRadius = cornerRadius,
            shadowColor = shadowColor,
            offsetX = shadowOffsetX,
            offsetY = shadowOffsetY
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
        .graphicsLayer {
            translationX = travelX.toPx() * pressFraction
            translationY = travelY.toPx() * pressFraction
        }
        .clip(shape)
        .background(containerColor, shape)
        .border(BorderStroke(borderWidth, borderColor), shape)
}

/**
 * Draws a subtle dot-matrix hardware grid across the background canvas.
 */
fun Modifier.dotGridBackground(
    dotColor: Color = Color.White.copy(alpha = 0.055f),
    spacing: Dp = 18.dp,
    dotRadius: Dp = 1.dp
): Modifier = this.drawBehind {
    val step = spacing.toPx()
    val radius = dotRadius.toPx()
    if (step <= 0f) return@drawBehind
    var x = step * 0.5f
    while (x < size.width) {
        var y = step * 0.5f
        while (y < size.height) {
            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(x, y)
            )
            y += step
        }
        x += step
    }
}
