package com.ben0551.visualtimeassistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * D-Pad affordance: scales the element and draws a bright amber outline plus a soft tint
 * while focused. Apply to any clickable card/button so TV users always see where they are.
 */
@Composable
fun Modifier.tvFocusScale(
    scale: Float = 1.08f,
    borderWidth: Dp = 5.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 16.dp,
): Modifier {
    var focused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (focused) scale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "tvFocusScale",
    )
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .onFocusChanged { state -> focused = state.isFocused }
        .clip(shape)
        .background(if (focused) borderColor.copy(alpha = 0.30f) else Color.Transparent, shape)
        .border(
            BorderStroke(borderWidth, if (focused) borderColor else Color.Transparent),
            shape,
        )
        .scale(animatedScale)
}

/** Small helper for stacking a focused border without scaling. */
@Composable
fun Modifier.tvFocusBorder(
    borderWidth: Dp = 4.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 16.dp,
): Modifier {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .onFocusChanged { state -> focused = state.isFocused }
        .clip(shape)
        .border(
            BorderStroke(borderWidth, if (focused) borderColor else Color.Transparent),
            shape,
        )
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) { content() }
}
