package com.ben0551.visualtimeassistant.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ben0551.visualtimeassistant.domain.model.Preset
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.ui.components.tvFocusScale
import com.ben0551.visualtimeassistant.util.DurationFormatter

private val QUICK_DURATIONS = listOf(5 * 60, 10 * 60, 15 * 60, 30 * 60)

/**
 * Screen 1 - launch a timer within two interactions.
 * Large cards work across a room and are fully D-Pad navigable.
 */
@Composable
fun HomeScreen(
    onStartDuration: (seconds: Int, theme: ThemeId) -> Unit,
    onOpenCustom: () -> Unit,
    onOpenPresets: () -> Unit,
    onStartPreset: (Preset) -> Unit,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Start with the first quick-start card focused so the remote user can see where they are.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 170.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        "Visual Time Assistant",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Pick a timer and go.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionLabel("QUICK START")
        }

        items(QUICK_DURATIONS) { seconds ->
            QuickCard(
                title = "${seconds / 60} MINUTES",
                subtitle = DurationFormatter.clock(seconds),
                initialFocus = seconds == QUICK_DURATIONS.first(),
                onClick = { onStartDuration(seconds, settings.theme) },
            )
        }

        item {
            QuickCard(
                title = "CUSTOM",
                subtitle = "Set your own time",
                onClick = onOpenCustom,
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionLabel("MY PRESETS")
        }

        if (presets.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(onClick = onOpenPresets, modifier = Modifier.fillMaxWidth().tvFocusScale()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("No presets yet", fontWeight = FontWeight.Bold)
                        Text(
                            "Open Presets to create routines like Morning Routine or Bed Time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(presets, key = { it.id }) { preset ->
                PresetCard(preset = preset, onClick = { onStartPreset(preset) })
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(onClick = onOpenPresets, modifier = Modifier.fillMaxWidth().tvFocusScale()) {
                Text(
                    "Manage presets",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun QuickCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    initialFocus: Boolean = false,
    onClick: () -> Unit,
) {
    // Requesting focus from inside the item means the requester is already attached.
    // The navigation rail grabs focus during its own initialisation, so keep trying:
    // on a TV the first thing the user must see is where the remote is.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(initialFocus) {
        if (!initialFocus) return@LaunchedEffect
        repeat(30) {
            val requested = runCatching { focusRequester.requestFocus() }.isSuccess
            if (requested) return@LaunchedEffect
            kotlinx.coroutines.delay(50)
        }
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .focusRequester(focusRequester)
            .focusGroup()
            .tvFocusScale(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PresetCard(preset: Preset, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().tvFocusScale(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(preset.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        DurationFormatter.clock(preset.durationSec),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (preset.isFavourite) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Favourite",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Icon(
                        Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    preset.theme.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
