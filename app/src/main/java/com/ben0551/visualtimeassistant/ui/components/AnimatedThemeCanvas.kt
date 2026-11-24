package com.ben0551.visualtimeassistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Full-screen low-distraction animated environment. [phase] drives continuous motion
 * (clouds, bubbles, flames) and is zeroed when reduced motion is enabled.
 */
@Composable
fun AnimatedThemeBackground(
    theme: ThemeId,
    progress: Float,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "themePhase")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 6000, easing = LinearEasing)),
        label = "phase",
    )
    val motion = if (reducedMotion) 0f else phase
    val stars = remember { StarField(seed = 7, count = 42) }

    Canvas(modifier = modifier) {
        when (theme) {
            ThemeId.FOREST -> drawForest(progress, motion)
            ThemeId.ROCKET -> drawRocket(progress, motion, stars)
            ThemeId.AQUARIUM -> drawAquarium(progress, motion)
            ThemeId.SPACE -> drawSpace(progress, motion, stars)
            ThemeId.POTATO -> drawPotatoEmpire(progress, motion)
            // Callers resolve RANDOM before rendering; forest is the safe fallback.
            ThemeId.RANDOM -> drawForest(progress, motion)
        }
    }
}

private class StarField(seed: Int, count: Int) {
    val points: List<Triple<Float, Float, Float>> =
        List(count) { i ->
            val random = Random(seed * 1000 + i)
            Triple(random.nextFloat(), random.nextFloat(), 1f + random.nextFloat() * 2.5f)
        }
}

private fun DrawScope.drawStars(stars: StarField) {
    stars.points.forEach { (x, y, r) ->
        drawCircle(Color.White.copy(alpha = 0.7f), radius = r, center = Offset(size.width * x, size.height * y))
    }
}

// ---------------------------------------------------------------- FOREST

private fun DrawScope.drawForest(progress: Float, motion: Float) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFF1B3A57), Color(0xFF0B1620)), endY = h))
    drawCircle(Color(0xFFFFE082), radius = w * 0.05f, center = Offset(w * 0.83f, h * 0.15f))

    // drifting clouds
    val cloudX = ((motion * w * 0.4f) % (w * 1.4f)) - w * 0.2f
    drawCloud(Offset(cloudX, h * 0.18f), w * 0.09f)
    drawCloud(Offset(w * 0.65f - cloudX * 0.5f, h * 0.28f), w * 0.06f)

    val groundTop = h * 0.70f
    drawRect(Color(0xFF24402C), Offset(0f, groundTop), Size(w, h - groundTop))

    val cx = w * 0.5f
    val baseY = groundTop + h * 0.06f
    when {
        progress < 0.2f -> drawOval(
            Color(0xFF5D4037),
            topLeft = Offset(cx - w * 0.035f, baseY - h * 0.03f),
            size = Size(w * 0.07f, h * 0.045f),
        )
        else -> {
            val stemTop = baseY - (h * 0.06f + progress * h * 0.32f)
            drawLine(
                Color(0xFF66BB6A),
                start = Offset(cx, baseY),
                end = Offset(cx, stemTop),
                strokeWidth = w * 0.014f,
                cap = StrokeCap.Round,
            )
            val leafRadius = w * (0.024f + 0.022f * progress)
            drawCircle(Color(0xFF4CAF50), leafRadius, Offset(cx - leafRadius * 1.0f, baseY + (stemTop - baseY) * 0.55f))
            drawCircle(Color(0xFF81C784), leafRadius * 0.85f, Offset(cx + leafRadius * 1.0f, baseY + (stemTop - baseY) * 0.4f))
            if (progress >= 0.75f) {
                drawCircle(Color(0xFF2E7D32), w * 0.10f, Offset(cx, stemTop))
                drawCircle(Color(0xFF43A047), w * 0.075f, Offset(cx - w * 0.07f, stemTop + h * 0.03f))
                drawCircle(Color(0xFF66BB6A), w * 0.075f, Offset(cx + w * 0.07f, stemTop + h * 0.03f))
            } else if (progress >= 0.45f) {
                drawCircle(Color(0xFFFFC107), w * 0.025f, Offset(cx, stemTop))
            }
        }
    }
}

private fun DrawScope.drawCloud(center: Offset, radius: Float) {
    drawCircle(Color.White.copy(alpha = 0.18f), radius, center)
    drawCircle(Color.White.copy(alpha = 0.18f), radius * 0.75f, Offset(center.x + radius, center.y + radius * 0.15f))
    drawCircle(Color.White.copy(alpha = 0.18f), radius * 0.6f, Offset(center.x - radius, center.y + radius * 0.2f))
}

// ---------------------------------------------------------------- ROCKET

private fun DrawScope.drawRocket(progress: Float, motion: Float, stars: StarField) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFF2A1B4A), Color(0xFF05070F)), endY = h))
    drawStars(stars)

    drawRect(Color(0xFF372C4A), Offset(0f, h * 0.88f), Size(w, h * 0.12f))
    // launch tower
    drawRect(Color(0xFF546E7A), Offset(w * 0.40f, h * 0.55f), Size(w * 0.02f, h * 0.33f))

    val cx = w * 0.52f
    val rise = progress * h * 0.5f
    val baseY = h * 0.88f - rise
    val bodyWidth = w * 0.07f
    val bodyHeight = h * 0.22f
    val bodyTopLeft = Offset(cx - bodyWidth / 2f, baseY - bodyHeight)

    // flame
    val flicker = if (motion == 0f) 1f else 0.65f + 0.35f * abs(sin(motion * Math.PI.toFloat() * 4f))
    val flameHeight = bodyHeight * 0.55f * flicker
    val flame = Path().apply {
        moveTo(cx - bodyWidth * 0.22f, baseY)
        lineTo(cx + bodyWidth * 0.22f, baseY)
        lineTo(cx, baseY + flameHeight)
        close()
    }
    drawPath(flame, Color(0xFFFFA726))
    drawPath(flame, Color(0xFFFFE082).copy(alpha = 0.7f).let { it })

    // fins
    val finLeft = Path().apply {
        moveTo(bodyTopLeft.x, baseY - bodyHeight * 0.35f)
        lineTo(bodyTopLeft.x - bodyWidth * 0.35f, baseY)
        lineTo(bodyTopLeft.x, baseY)
        close()
    }
    drawPath(finLeft, Color(0xFFEF5350))
    val finRight = Path().apply {
        moveTo(bodyTopLeft.x + bodyWidth, baseY - bodyHeight * 0.35f)
        lineTo(bodyTopLeft.x + bodyWidth * 1.35f, baseY)
        lineTo(bodyTopLeft.x + bodyWidth, baseY)
        close()
    }
    drawPath(finRight, Color(0xFFEF5350))

    // body + nose + window
    drawRoundRect(
        Color.White,
        topLeft = bodyTopLeft,
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyWidth / 2f, bodyHeight / 4f),
    )
    val nose = Path().apply {
        moveTo(bodyTopLeft.x, bodyTopLeft.y + bodyHeight * 0.05f)
        lineTo(cx, bodyTopLeft.y - bodyHeight * 0.5f)
        lineTo(bodyTopLeft.x + bodyWidth, bodyTopLeft.y + bodyHeight * 0.05f)
        close()
    }
    drawPath(nose, Color(0xFFEF5350))
    drawCircle(Color(0xFF4FC3F7), bodyWidth * 0.22f, Offset(cx, bodyTopLeft.y + bodyHeight * 0.35f))
}

// ---------------------------------------------------------------- AQUARIUM

private fun DrawScope.drawAquarium(progress: Float, motion: Float) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFF0E4D6E), Color(0xFF062033)), endY = h))

    // light rays
    drawPath(
        Path().apply {
            moveTo(w * 0.15f, 0f)
            lineTo(w * 0.30f, 0f)
            lineTo(w * 0.42f, h)
            lineTo(w * 0.20f, h)
            close()
        },
        Color.White.copy(alpha = 0.05f),
    )

    drawRect(Color(0xFFC2B280), Offset(0f, h * 0.88f), Size(w, h * 0.12f))

    // seaweed
    repeat(4) { i ->
        val baseX = w * (0.10f + 0.25f * i)
        val sway = if (motion == 0f) 0f else sin(motion * Math.PI.toFloat() * 2f + i) * w * 0.015f
        val blade = Path().apply {
            moveTo(baseX, h * 0.9f)
            quadraticBezierTo(baseX + sway, h * 0.78f, baseX - sway, h * 0.68f)
            quadraticBezierTo(baseX + sway * 1.5f, h * 0.78f, baseX + w * 0.012f, h * 0.9f)
            close()
        }
        drawPath(blade, Color(0xFF2E7D32).copy(alpha = 0.85f))
    }

    // fish appear as time passes
    val fishColors = listOf(Color(0xFFFF7043), Color(0xFF4DD0E1), Color(0xFFFFC107), Color(0xFFBA68C8))
    val fishCount = 1 + (progress * 4).toInt().coerceIn(0, 4)
    repeat(fishCount) { i ->
        val bob = if (motion == 0f) 0f else sin(motion * Math.PI.toFloat() * 2f + i * 1.7f) * h * 0.02f
        val fishX = w * (0.15f + 0.22f * i) + (if (motion == 0f) 0f else sin(motion * 2f + i) * w * 0.01f)
        val fishY = h * (0.30f + ((i * 37) % 45) / 100f) + bob
        drawFish(Offset(fishX, fishY), w * 0.055f, fishColors[i % fishColors.size])
    }

    // bubbles
    val bubbleCount = 3 + (progress * 7).toInt()
    repeat(bubbleCount) { i ->
        val bx = w * (((i * 53) % 97) / 97f)
        val span = h * 0.7f
        val by = h * 0.9f - ((motion * span + i * 97f) % span)
        drawCircle(Color.White.copy(alpha = 0.5f), radius = 2f + (i % 3) * 1.5f, center = Offset(bx, by))
    }
}

private fun DrawScope.drawFish(center: Offset, length: Float, color: Color) {
    drawOval(color, topLeft = Offset(center.x - length / 2f, center.y - length / 4f), size = Size(length, length / 2f))
    val tail = Path().apply {
        moveTo(center.x + length / 2f, center.y)
        lineTo(center.x + length * 0.75f, center.y - length * 0.3f)
        lineTo(center.x + length * 0.75f, center.y + length * 0.3f)
        close()
    }
    drawPath(tail, color)
    drawCircle(Color.White, radius = length * 0.07f, center = Offset(center.x - length * 0.3f, center.y - length * 0.08f))
    drawCircle(Color.Black, radius = length * 0.035f, center = Offset(center.x - length * 0.3f, center.y - length * 0.08f))
}

// ---------------------------------------------------------------- SPACE

private fun DrawScope.drawSpace(progress: Float, motion: Float, stars: StarField) {
    val w = size.width
    val h = size.height
    drawRect(Color(0xFF05070F))
    drawStars(stars)

    val planetCenter = Offset(w * 0.86f, h * 0.32f)
    drawCircle(Color(0xFF8D5A3B), radius = w * 0.10f, center = planetCenter)
    drawCircle(Color(0xFFB76E3B).copy(alpha = 0.5f), radius = w * 0.13f, center = planetCenter)
    drawOval(
        Color(0xFFD7CCC8).copy(alpha = 0.35f),
        topLeft = Offset(planetCenter.x - w * 0.20f, planetCenter.y - w * 0.03f),
        size = Size(w * 0.40f, w * 0.06f),
    )

    val start = Offset(w * 0.10f, h * 0.62f)
    val end = Offset(w * 0.72f, h * 0.34f)
    val position = Offset(
        start.x + (end.x - start.x) * progress,
        start.y + (end.y - start.y) * progress - sin(progress * Math.PI.toFloat()) * h * 0.20f,
    )
    // trail
    drawLine(
        Color(0xFF4DD0E1).copy(alpha = 0.35f),
        start = start,
        end = position,
        strokeWidth = w * 0.008f,
        cap = StrokeCap.Round,
    )

    val velocity = Offset(
        (end.x - start.x) / w,
        ((end.y - start.y) - cos(progress * Math.PI.toFloat()) * Math.PI.toFloat() * h * 0.20f) / w,
    )
    val angleDeg = Math.toDegrees(atan2(velocity.y.toDouble(), velocity.x.toDouble())).toFloat()
    rotate(degrees = angleDeg, pivot = position) {
        val shipLength = w * 0.06f
        val body = Path().apply {
            moveTo(position.x + shipLength / 2f, position.y)
            lineTo(position.x - shipLength / 2f, position.y - shipLength * 0.25f)
            lineTo(position.x - shipLength / 2f, position.y + shipLength * 0.25f)
            close()
        }
        drawPath(body, Color(0xFFECEFF1))
        drawCircle(Color(0xFF4FC3F7), radius = shipLength * 0.12f, center = Offset(position.x, position.y))
        if (motion != 0f) {
            drawLine(
                Color(0xFFFFA726),
                start = Offset(position.x - shipLength / 2f, position.y),
                end = Offset(position.x - shipLength * 0.8f, position.y),
                strokeWidth = shipLength * 0.15f,
                cap = StrokeCap.Round,
            )
        }
    }
}

// ---------------------------------------------------------------- POTATO EMPIRE

private fun DrawScope.drawPotatoEmpire(progress: Float, motion: Float) {
    val w = size.width
    val h = size.height
    drawRect(Brush.verticalGradient(listOf(Color(0xFF3E2A18), Color(0xFF231A12)), endY = h))
    // soil texture
    repeat(12) { i ->
        val random = Random(i)
        drawOval(
            Color(0xFF1A120C).copy(alpha = 0.6f),
            topLeft = Offset(w * random.nextFloat(), h * random.nextFloat()),
            size = Size(w * 0.05f, h * 0.02f),
        )
    }

    val groundY = h * 0.78f
    drawRect(Color(0xFF4E342E), Offset(0f, groundY), Size(w, h - groundY))

    when {
        progress < 0.2f -> Unit
        progress < 0.4f -> drawPotato(Offset(w * 0.5f, groundY), w * 0.09f)
        progress < 0.6f -> {
            drawFence()
            drawPotato(Offset(w * 0.35f, groundY), w * 0.07f)
            drawPotato(Offset(w * 0.5f, groundY + h * 0.02f), w * 0.08f)
            drawPotato(Offset(w * 0.65f, groundY), w * 0.07f)
        }
        progress < 0.8f -> {
            drawFence()
            repeat(2) { row -> repeat(3) { col ->
                drawPotato(Offset(w * (0.3f + 0.2f * col), groundY + h * 0.03f * row), w * 0.065f)
            } }
        }
        else -> drawEmpire(motion)
    }
}

private fun DrawScope.drawFence() {
    val w = size.width
    val h = size.height
    val y = h * 0.78f
    drawRect(Color(0xFF8D6E63), Offset(w * 0.2f, y - h * 0.02f), Size(w * 0.6f, h * 0.012f))
    repeat(5) { i ->
        drawRect(Color(0xFF6D4C41), Offset(w * (0.22f + 0.13f * i), y - h * 0.06f), Size(w * 0.012f, h * 0.05f))
    }
}

private fun DrawScope.drawPotato(center: Offset, radius: Float) {
    drawOval(Color(0xFFC89F6B), topLeft = Offset(center.x - radius, center.y - radius * 0.7f), size = Size(radius * 2f, radius * 1.4f))
    drawCircle(Color(0xFF8D6E63), radius * 0.12f, Offset(center.x - radius * 0.4f, center.y - radius * 0.2f))
    drawCircle(Color(0xFF8D6E63), radius * 0.1f, Offset(center.x + radius * 0.3f, center.y + radius * 0.1f))
}

private fun DrawScope.drawEmpire(motion: Float) {
    val w = size.width
    val h = size.height
    val groundY = h * 0.82f

    // central keep
    drawRect(Color(0xFF8D6E63), Offset(w * 0.44f, groundY - h * 0.24f), Size(w * 0.12f, h * 0.24f))
    drawOval(Color(0xFFC89F6B), topLeft = Offset(w * 0.42f, groundY - h * 0.32f), size = Size(w * 0.16f, h * 0.12f))
    // towers
    listOf(0.28f, 0.66f).forEach { fx ->
        drawRect(Color(0xFF6D4C41), Offset(w * fx, groundY - h * 0.18f), Size(w * 0.06f, h * 0.18f))
        drawOval(Color(0xFFC89F6B), topLeft = Offset(w * (fx - 0.01f), groundY - h * 0.24f), size = Size(w * 0.08f, h * 0.09f))
        // flag
        val wave = if (motion == 0f) 0f else sin(motion * Math.PI.toFloat() * 3f) * w * 0.01f
        drawRect(Color(0xFF455A64), Offset(w * fx + w * 0.027f, groundY - h * 0.34f), Size(w * 0.004f, h * 0.10f))
        drawPath(
            Path().apply {
                moveTo(w * fx + w * 0.031f, groundY - h * 0.34f)
                lineTo(w * fx + w * 0.075f + wave, groundY - h * 0.315f)
                lineTo(w * fx + w * 0.031f, groundY - h * 0.29f)
                close()
            },
            Color(0xFFFFC107),
        )
    }
    // potato crop rows
    repeat(2) { row -> repeat(4) { col ->
        drawPotato(Offset(w * (0.34f + 0.1f * col), groundY + h * 0.04f * row), w * 0.035f)
    } }
}
