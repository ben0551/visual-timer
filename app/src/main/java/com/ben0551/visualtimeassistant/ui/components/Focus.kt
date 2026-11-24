package com.ben0551.visualtimeassistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draw the D-pad focus highlight as an outline painted over the content.
 *
 * The outline is drawn after `drawContent`, so it stays visible on controls that paint their
 * own container colour (buttons, cards, chips) - a background/border modifier placed on the
 * outside of those components gets covered by their fill and the focus state disappears.
 *
 * Focus can come from either the component's own [MutableInteractionSource] (passed in by the
 * caller and handed to the clickable control) or ancestor focus events; interaction sources
 * proved necessary because toggleable rows in a lazy list did not report focus through
 * ancestor observation.
 */
@Composable
fun Modifier.tvFocusScale(
    scale: Float = 1.08f,
    borderWidth: Dp = 5.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 16.dp,
    interactionSource: MutableInteractionSource? = null,
): Modifier {
    var eventFocused by remember { mutableStateOf(false) }
    val sourceFocused = interactionSource?.collectIsFocusedAsState()?.value ?: false
    val focused = sourceFocused || eventFocused
    val animatedScale by animateFloatAsState(
        targetValue = if (focused) scale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "tvFocusScale",
    )
    return this
        .onFocusChanged { state -> eventFocused = state.isFocused }
        .scale(animatedScale)
        .drawWithContent {
            drawContent()
            if (focused) {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        cornerRadius.toPx(),
                        cornerRadius.toPx(),
                    ),
                    style = Stroke(width = borderWidth.toPx()),
                )
            }
        }
}

/** Shape helper kept for callers that clip to the same corner radius as the highlight. */
val tvFocusShape: RoundedCornerShape get() = RoundedCornerShape(16.dp)
