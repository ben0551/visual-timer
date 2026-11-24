package com.ben0551.visualtimeassistant.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Single dark-first theme shared by TV, tablet and phone.
 * Dark surfaces keep the visuals readable from across a room and avoid glare at bedtime.
 *
 * Material 3 does not seed `LocalContentColor` from the scheme, so uncoloured `Text`
 * composables render black - unreadable on these dark surfaces. Providing it explicitly
 * makes every default-coloured label light, while still letting screens override per text.
 */
@Composable
fun VisualTimeAssistantTheme(
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (highContrast) HighContrastColorScheme else DefaultColorScheme
    CompositionLocalProvider(LocalContentColor provides colorScheme.onBackground) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
