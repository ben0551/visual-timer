package com.ben0551.visualtimeassistant.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.ben0551.visualtimeassistant.data.parseMilestones
import com.ben0551.visualtimeassistant.domain.model.AppSettings
import com.ben0551.visualtimeassistant.domain.model.DisplayMode
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerLimits

@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        item { SectionTitle("DISPLAY") }
        item {
            SwitchRow(
                title = "Show countdown",
                subtitle = "Large remaining-time number",
                checked = settings.showCountdown,
                onChange = { value -> viewModel.update { it.copy(showCountdown = value) } },
            )
        }
        item {
            SwitchRow(
                title = "Show percentage",
                subtitle = "Percent of time left under the progress display",
                checked = settings.showPercentage,
                onChange = { value -> viewModel.update { it.copy(showPercentage = value) } },
            )
        }
        item {
            SwitchRow(
                title = "Show progress bar",
                subtitle = "Elapsed-time growth bar",
                checked = settings.showProgressBar,
                onChange = { value -> viewModel.update { it.copy(showProgressBar = value) } },
            )
        }
        item { ChoiceRow(
            title = "Progress style",
            options = DisplayMode.entries.map { it.name to it.label },
            selectedKey = settings.displayMode.name,
            onSelect = { key -> viewModel.update { it.copy(displayMode = DisplayMode.valueOf(key)) } },
        ) }

        item { SectionTitle("THEME") }
        item { ChoiceRow(
            title = "Animated environment",
            options = ThemeId.selectable().map { it.name to it.displayName },
            selectedKey = settings.theme.name,
            onSelect = { key -> viewModel.update { it.copy(theme = ThemeId.valueOf(key) ) } },
        ) }

        item { SectionTitle("NOTIFICATIONS") }
        item {
            SwitchRow(
                title = "Voice prompts",
                subtitle = "Spoken announcements at milestones",
                checked = settings.voiceEnabled,
                onChange = { value -> viewModel.update { it.copy(voiceEnabled = value) } },
            )
        }
        item { MilestonesEditor(
            milestones = settings.customMilestones,
            onApply = { values -> viewModel.update { it.copy(customMilestones = values) } },
        ) }
        item {
            SwitchRow(
                title = "Dad Mode",
                subtitle = "Extra urgency layer near the end",
                checked = settings.dadModeEnabled,
                onChange = { value -> viewModel.update { it.copy(dadModeEnabled = value) } },
            )
        }
        if (settings.dadModeEnabled) {
            item { DadModeThresholdRow(
                thresholdPercent = settings.dadModeThresholdPercent,
                onChange = { value -> viewModel.update { it.copy(dadModeThresholdPercent = value) } },
            ) }
        }

        item { SectionTitle("ACCESSIBILITY") }
        item { SwitchRow("High contrast", "Pure black and white surfaces",
            settings.highContrast) { v -> viewModel.update { it.copy(highContrast = v) } } }
        item { SwitchRow("Large text", "Bigger countdown and labels",
            settings.largeText) { v -> viewModel.update { it.copy(largeText = v) } } }
        item { SwitchRow("Reduced motion", "No flashing or animated movement",
            settings.reducedMotion) { v -> viewModel.update { it.copy(reducedMotion = v) } } }
        item { SwitchRow("Colour-blind palette", "Okabe-Ito hues instead of green-amber-red",
            settings.colourBlindPalette) { v -> viewModel.update { it.copy(colourBlindPalette = v) } } }
        item { SwitchRow("Mute mode", "Silence every sound including voice",
            settings.muteMode) { v -> viewModel.update { it.copy(muteMode = v) } } }

        item {
            OutlinedButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("RESET TO DEFAULTS")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** Chip row for enum-like choices. */
@Composable
private fun ChoiceRow(
    title: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
) {
    Column {
        Text(title, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { (key, label) ->
                FilterChip(
                    selected = selectedKey == key,
                    onClick = { onSelect(key) },
                    label = { Text(label) },
                )
            }
        }
    }
}

@Composable
private fun MilestonesEditor(
    milestones: List<Int>,
    onApply: (List<Int>) -> Unit,
) {
    var text by remember(milestones) { mutableStateOf(milestones.joinToString(", ")) }
    var error by remember { mutableStateOf<String?>(null) }

    Column {
        Text("Custom voice milestones", fontWeight = FontWeight.Bold)
        Text(
            "Percentages that trigger a spoken announcement, e.g. 20, 40, 60, 80.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { raw ->
                text = raw
                val parsed = parseMilestones(raw)
                error = when {
                    parsed.isEmpty() && raw.isNotBlank() -> "Use numbers between 1 and 100."
                    else -> null
                }
            },
            isError = error != null,
            label = { Text("Percentages") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("20, 40, 60, 80" to listOf(20, 40, 60, 80), "25, 50, 75" to listOf(25, 50, 75)).forEach { (label, values) ->
                FilterChip(
                    selected = milestones == values,
                    onClick = {
                        text = values.joinToString(", ")
                        error = null
                        onApply(values)
                    },
                    label = { Text(label) },
                )
            }
            Button(
                enabled = error == null && parseMilestones(text).isNotEmpty(),
                onClick = { onApply(parseMilestones(text)) },
            ) {
                Text("APPLY")
            }
        }
    }
}

@Composable
private fun DadModeThresholdRow(
    thresholdPercent: Int,
    onChange: (Int) -> Unit,
) {
    var draft by remember(thresholdPercent) { mutableStateOf(thresholdPercent.toFloat()) }
    Column {
        Text("Dad Mode threshold: ${draft.toInt()}%", fontWeight = FontWeight.Bold)
        Slider(
            value = draft,
            onValueChange = { draft = it },
            onValueChangeFinished = { onChange(draft.toInt().coerceIn(
                TimerLimits.DAD_MODE_MIN_THRESHOLD_PERCENT,
                TimerLimits.DAD_MODE_MAX_THRESHOLD_PERCENT,
            )) },
            valueRange = TimerLimits.DAD_MODE_MIN_THRESHOLD_PERCENT.toFloat()..TimerLimits.DAD_MODE_MAX_THRESHOLD_PERCENT.toFloat(),
            steps = TimerLimits.DAD_MODE_MAX_THRESHOLD_PERCENT - TimerLimits.DAD_MODE_MIN_THRESHOLD_PERCENT - 1,
        )
        Text(
            "Lower percentages mean a calmer timer; 5% is the default.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
