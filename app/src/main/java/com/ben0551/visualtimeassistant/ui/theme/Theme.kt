package com.ben0551.visualtimeassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Single dark-first theme shared by TV, tablet and phone.
 * Dark surfaces keep the visuals readable from across a room and avoid glare at bedtime.
 */
@Composable
fun VisualTimeAssistantTheme(
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (highContrast) HighContrastColorScheme else DefaultColorScheme,
        content = content,
    )
}
