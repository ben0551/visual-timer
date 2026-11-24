package com.ben0551.visualtimeassistant.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Core brand colours
val Amber = Color(0xFFFFC107)
val Green = Color(0xFF4CAF50)
val Red = Color(0xFFFF5252)
val DeepNavy = Color(0xFF0B1620)
val SurfaceNavy = Color(0xFF12222E)
val SurfaceVariantNavy = Color(0xFF1B3242)
val Teal = Color(0xFF4DD0E1)

val DefaultColorScheme = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1A1200),
    primaryContainer = Color(0xFF3E2E00),
    onPrimaryContainer = Amber,
    secondary = Teal,
    onSecondary = Color(0xFF002026),
    tertiary = Green,
    onTertiary = Color(0xFF00210B),
    background = DeepNavy,
    onBackground = Color(0xFFF2F7FA),
    surface = SurfaceNavy,
    onSurface = Color(0xFFF2F7FA),
    surfaceVariant = SurfaceVariantNavy,
    onSurfaceVariant = Color(0xFFB9CBD6),
    outline = Color(0xFF33505F),
    error = Red,
    onError = Color(0xFF2B0000),
)

/** Extra legibility for bright living-room TVs. */
val HighContrastColorScheme = darkColorScheme(
    primary = Color(0xFFFFD54A),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF5C4300),
    onPrimaryContainer = Color(0xFFFFD54A),
    secondary = Color(0xFF80E8FF),
    tertiary = Color(0xFF8AFFA0),
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color.White,
    outline = Color.White,
    error = Color(0xFFFF6E6E),
)

/**
 * Progress colour ramp. Elapsed time grows green -> amber -> red so urgency reads instantly.
 * The colour-blind ramp uses Okabe-Ito hues (bluish green / orange / vermilion).
 */
object ProgressPalette {
    private val standard = listOf(
        0.0f to Color(0xFF4CAF50),
        0.5f to Color(0xFFFFC107),
        1.0f to Color(0xFFFF5252),
    )

    private val colourBlind = listOf(
        0.0f to Color(0xFF009E73),
        0.5f to Color(0xFFE69F00),
        1.0f to Color(0xFFD55E00),
    )

    fun color(fraction: Float, colourBlindPalette: Boolean): Color {
        val stops = if (colourBlindPalette) colourBlind else standard
        val clamped = fraction.coerceIn(0f, 1f)
        val upper = stops.indexOfFirst { clamped <= it.first }.let { if (it == -1) stops.lastIndex else it }
        val lower = (upper - 1).coerceAtLeast(0)
        val (startFraction, startColor) = stops[lower]
        val (endFraction, endColor) = stops[upper]
        val span = (endFraction - startFraction).takeIf { it > 0f } ?: 1f
        val local = ((clamped - startFraction) / span).coerceIn(0f, 1f)
        return lerp(startColor, endColor, local)
    }
}
