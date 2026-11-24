package com.ben0551.visualtimeassistant.ui.presets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ben0551.visualtimeassistant.domain.model.Preset
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerLimits
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import com.ben0551.visualtimeassistant.ui.components.StepperRow
import com.ben0551.visualtimeassistant.ui.components.TvFilterChip
import com.ben0551.visualtimeassistant.ui.components.tvFocusScale
import com.ben0551.visualtimeassistant.util.DurationFormatter

/** Parent-editable preset library. */
@Composable
fun PresetsScreen(
    onBack: () -> Unit,
    onStartPreset: (Preset) -> Unit,
) {
    val viewModel: PresetsViewModel = hiltViewModel()
    val presets by viewModel.presets.collectAsStateWithLifecycle()

    // Editor target: preset draft plus whether it is new.
    var editorDraft by remember { mutableStateOf<Pair<Preset, Boolean>?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    "Presets",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Routines your family uses every day.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        editorDraft = Pair(
                            Preset(
                                name = "",
                                durationSec = 5 * 60,
                                theme = ThemeId.FOREST,
                                warningProfile = WarningProfileId.DEFAULT,
                            ),
                            true,
                        )
                    },
                    modifier = Modifier.height(56.dp).tvFocusScale(cornerRadius = 18.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("NEW PRESET")
                }
            }
        }

        items(presets, key = { it.id }) { preset ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(preset.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${DurationFormatter.clock(preset.durationSec)}  ·  ${preset.theme.displayName}  ·  ${preset.warningProfile.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onStartPreset(preset) }) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "Start ${preset.name}",
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFavourite(preset) }) {
                        if (preset.isFavourite) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Remove favourite",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.StarOutline,
                                contentDescription = "Make favourite",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = { editorDraft = Pair(preset, false) }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit ${preset.name}")
                    }
                    IconButton(onClick = { viewModel.delete(preset) }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete ${preset.name}",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    editorDraft?.let { (draft, isNew) ->
        PresetEditorDialog(
            initial = draft,
            isNew = isNew,
            onDismiss = { editorDraft = null },
            onSave = { saved ->
                viewModel.save(saved)
                editorDraft = null
            },
            onDelete = { toDelete ->
                viewModel.delete(toDelete)
                editorDraft = null
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetEditorDialog(
    initial: Preset,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Preset) -> Unit,
    onDelete: (Preset) -> Unit,
) {
    var name by remember { mutableStateOf(initial.name) }
    var hours by remember { mutableStateOf(initial.durationSec / 3600) }
    var minutes by remember { mutableStateOf((initial.durationSec % 3600) / 60) }
    var seconds by remember { mutableStateOf(initial.durationSec % 60) }
    var theme by remember { mutableStateOf(initial.theme) }
    var profile by remember { mutableStateOf(initial.warningProfile) }
    var favourite by remember { mutableStateOf(initial.isFavourite) }

    val totalSeconds = hours * 3600 + minutes * 60 + seconds
    val error = when {
        name.isBlank() -> "Give the preset a name."
        totalSeconds < TimerLimits.MIN_SECONDS -> "Minimum is 10 seconds."
        totalSeconds > TimerLimits.MAX_SECONDS -> "Maximum is 24 hours."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "New preset" else "Edit preset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                StepperRow(
                    label = "HOURS",
                    valueText = "%02d".format(hours),
                    onMinus = { hours = (hours - 1).coerceIn(0, 23) },
                    onPlus = { hours = (hours + 1).coerceIn(0, 23) },
                    buttonSize = 52.dp,
                )
                StepperRow(
                    label = "MINUTES",
                    valueText = "%02d".format(minutes),
                    onMinus = { minutes = (minutes - 1).coerceIn(0, 59) },
                    onPlus = { minutes = (minutes + 1).coerceIn(0, 59) },
                    buttonSize = 52.dp,
                )
                StepperRow(
                    label = "SECONDS",
                    valueText = "%02d".format(seconds),
                    onMinus = { seconds = (seconds - 5).coerceIn(0, 59) },
                    onPlus = { seconds = (seconds + 5).coerceIn(0, 59) },
                    buttonSize = 52.dp,
                )
                Text(
                    "THEME",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ThemeId.selectable().take(5).forEach { option ->
                        TvFilterChip(
                            selected = theme == option,
                            onClick = { theme = option },
                            label = option.displayName,
                        )
                    }
                }
                Text(
                    "WARNINGS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WarningProfileId.entries.forEach { option ->
                        TvFilterChip(
                            selected = profile == option,
                            onClick = { profile = option },
                            label = option.label,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = favourite, onCheckedChange = { favourite = it })
                    Text("Show on home screen first")
                }
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                } else {
                    Text("Total ${DurationFormatter.clock(totalSeconds)}", color = MaterialTheme.colorScheme.secondary)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = error == null,
                onClick = {
                    onSave(
                        initial.copy(
                            name = name.trim(),
                            durationSec = totalSeconds,
                            theme = theme,
                            warningProfile = profile,
                            isFavourite = favourite,
                        ),
                    )
                },
            ) { Text("SAVE") }
        },
        dismissButton = {
            Row {
                if (!isNew) {
                    TextButton(onClick = { onDelete(initial) }) {
                        Text("DELETE", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("CANCEL") }
            }
        },
    )
}
