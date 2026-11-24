package com.ben0551.visualtimeassistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Filled area = elapsed time (children read "how much has gone" more easily than remaining). */
@Composable
fun ElapsedProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    barHeight: Dp = 32.dp,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 150),
        label = "elapsedBar",
    )
    Canvas(modifier = modifier.fillMaxWidth().height(barHeight)) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(color = trackColor, cornerRadius = radius, size = size)
        if (animated > 0.001f) {
            drawRoundRect(
                color = color,
                cornerRadius = radius,
                size = Size(size.width * animated, size.height),
            )
        }
    }
}

/** Pie that fills as time elapses, with arbitrary content in the middle. */
@Composable
fun CircularCountdown(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 22.dp,
    center: @Composable () -> Unit = {},
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = Color.White.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * fraction.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        center()
    }
}

/** Ten block countdown: 🟩🟩🟩⬜⬜ as time runs out. */
@Composable
fun BlockCountdown(
    fraction: Float,
    blocks: Int = 10,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    blockHeight: Dp = 40.dp,
) {
    val filled = (fraction.coerceIn(0f, 1f) * blocks).roundToInt().coerceIn(0, blocks)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(blocks) { index ->
            val active = index < filled
            Box(
                Modifier
                    .weight(1f)
                    .height(blockHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            active -> color
                            else -> Color.White.copy(alpha = 0.15f)
                        },
                    ),
            )
        }
    }
}

/** Mode D: bar + percentage + remaining timer together. */
@Composable
fun CombinedProgress(
    fraction: Float,
    percentText: String,
    color: Color,
    modifier: Modifier = Modifier,
    largeText: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = percentText,
            style = if (largeText) {
                MaterialTheme.typography.displayMedium
            } else {
                MaterialTheme.typography.headlineMedium
            },
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Spacer(Modifier.height(12.dp))
        ElapsedProgressBar(fraction = fraction, color = color)
    }
}
