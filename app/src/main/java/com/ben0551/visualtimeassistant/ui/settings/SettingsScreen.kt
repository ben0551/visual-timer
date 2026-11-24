package com.ben0551.visualtimeassistant.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ben0551.visualtimeassistant.data.parseMilestones
import com.ben0551.visualtimeassistant.domain.model.DisplayMode
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerLimits
import com.ben0551.visualtimeassistant.ui.components.TvFilterChip
import com.ben0551.visualtimeassistant.ui.components.TvSecondaryButton
import com.ben0551.visualtimeassistant.ui.components.tvFocusScale

/**
 * Settings built for the remote: every option is either a whole-row toggle or a chip that
 * can be reached in one or two D-Pad moves - no tiny switches, no typing on a TV keyboard.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
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
            ToggleRow(
                title = "Show countdown",
                subtitle = "Large remaining-time number",
                checked = settings.showCountdown,
                onChange = { value -> viewModel.update { it.copy(showCountdown = value) } },
            )
        }
        item {
            ToggleRow(
                title = "Show percentage",
                subtitle = "Percent of time left under the progress display",
                checked = settings.showPercentage,
                onChange = { value -> viewModel.update { it.copy(showPercentage = value) } },
            )
        }
        item {
            ToggleRow(
                title = "Show progress bar",
                subtitle = "Elapsed-time growth bar",
                checked = settings.showProgressBar,
                onChange = { value -> viewModel.update { it.copy(showProgressBar = value) } },
            )
        }
        item {
            ChoiceRow(
                title = "Progress style",
                options = DisplayMode.entries.map { it.name to it.label },
                selectedKey = settings.displayMode.name,
                onSelect = { key -> viewModel.update { it.copy(displayMode = DisplayMode.valueOf(key)) } },
            )
        }

        item { SectionTitle("THEME") }
        item {
            ChoiceRow(
                title = "Animated environment",
                options = ThemeId.selectable().map { it.name to it.displayName },
                selectedKey = settings.theme.name,
                onSelect = { key -> viewModel.update { it.copy(theme = ThemeId.valueOf(key)) } },
            )
        }

        item { SectionTitle("NOTIFICATIONS") }
        item {
            ToggleRow(
                title = "Voice prompts",
                subtitle = "Spoken announcements at milestones",
                checked = settings.voiceEnabled,
                onChange = { value -> viewModel.update { it.copy(voiceEnabled = value) } },
            )
        }
        item {
            MilestonesEditor(
                milestones = settings.customMilestones,
                onApply = { values -> viewModel.update { it.copy(customMilestones = values) } },
            )
        }
        item {
            ToggleRow(
                title = "Dad Mode",
                subtitle = "Extra urgency layer near the end",
                checked = settings.dadModeEnabled,
                onChange = { value -> viewModel.update { it.copy(dadModeEnabled = value) } },
            )
        }
        if (settings.dadModeEnabled) {
            item {
                DadModeThresholdRow(
                    thresholdPercent = settings.dadModeThresholdPercent,
                    onChange = { value -> viewModel.update { it.copy(dadModeThresholdPercent = value) } },
                )
            }
        }
        item {
            ToggleRow(
                title = "Count overtime",
                subtitle = "Off: TIME IS UP ends the timer - no late tracking",
                checked = settings.overtimeEnabled,
                onChange = { value -> viewModel.update { it.copy(overtimeEnabled = value) } },
            )
        }

        item { SectionTitle("ACCESSIBILITY") }
        item {
            ToggleRow("High contrast", "Pure black and white surfaces", settings.highContrast) { v ->
                viewModel.update { it.copy(highContrast = v) }
            }
        }
        item {
            ToggleRow("Large text", "Bigger countdown and labels", settings.largeText) { v ->
                viewModel.update { it.copy(largeText = v) }
            }
        }
        item {
            ToggleRow("Reduced motion", "No flashing or animated movement", settings.reducedMotion) { v ->
                viewModel.update { it.copy(reducedMotion = v) }
            }
        }
        item {
            ToggleRow("Colour-blind palette", "Okabe-Ito hues instead of green-amber-red", settings.colourBlindPalette) { v ->
                viewModel.update { it.copy(colourBlindPalette = v) }
            }
        }
        item {
            ToggleRow("Mute mode", "Silence every sound including voice", settings.muteMode) { v ->
                viewModel.update { it.copy(muteMode = v) }
            }
        }

        item {
            TvSecondaryButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth().height(56.dp).tvFocusScale(cornerRadius = 16.dp),
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
        // Solid surface so content scrolling underneath the sticky header stays hidden.
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

/** Whole row toggles on OK - the switch itself is just the visual state. */
@Composable
private fun ToggleRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onValueChange = onChange,
            )
            .tvFocusScale(cornerRadius = 16.dp)
            .padding(horizontal = 12.dp),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (checked) "ON" else "OFF",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (checked) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}

/** Chips wrap across the screen so nothing hides off-scroll. */
@OptIn(ExperimentalLayoutApi::class)
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
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            options.forEach { (key, label) ->
                TvFilterChip(
                    selected = selectedKey == key,
                    onClick = { onSelect(key) },
                    label = label,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MilestonesEditor(
    milestones: List<Int>,
    onApply: (List<Int>) -> Unit,
) {
    var text by remember(milestones) { mutableStateOf(milestones.joinToString(", ")) }
    var error by remember { mutableStateOf<String?>(null) }

    val presets = listOf(
        "50 / 100" to listOf(50, 100),
        "25 / 50 / 75 / 100" to listOf(25, 50, 75, 100),
        "20 / 40 / 60 / 80" to listOf(20, 40, 60, 80),
        "Every 10%" to (10..90 step 10).toList(),
    )

    Column {
        Text("Voice milestone presets", fontWeight = FontWeight.Bold)
        Text(
            "One press applies a common set.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            presets.forEach { (label, values) ->
                TvFilterChip(
                    selected = milestones == values,
                    onClick = {
                        text = values.joinToString(", ")
                        error = null
                        onApply(values)
                    },
                    label = label,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Custom values", fontWeight = FontWeight.Bold)
        Text(
            "Comma separated percentages, e.g. 15, 30, 45.",
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
            // Remote focus must not get trapped in the text editor: the presets are the
            // primary path, and DPAD arrows would otherwise just move the caret.
            modifier = Modifier.fillMaxWidth().focusProperties { canFocus = false },
        )
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
        TvSecondaryButton(
            onClick = { onApply(parseMilestones(text)) },
            enabled = error == null && parseMilestones(text).isNotEmpty(),
            modifier = Modifier.height(48.dp).tvFocusScale(cornerRadius = 14.dp),
        ) {
            Text("APPLY CUSTOM")
        }
    }
}

/** Slider plus big +/- buttons: precise with a remote, fast with a touchscreen. */
@Composable
private fun DadModeThresholdRow(
    thresholdPercent: Int,
    onChange: (Int) -> Unit,
) {
    var draft by remember(thresholdPercent) { mutableStateOf(thresholdPercent.toFloat()) }
    val min = TimerLimits.DAD_MODE_MIN_THRESHOLD_PERCENT
    val max = TimerLimits.DAD_MODE_MAX_THRESHOLD_PERCENT

    fun commit(value: Int) {
        val clamped = value.coerceIn(min, max)
        draft = clamped.toFloat()
        onChange(clamped)
    }

    Column {
        Text("Dad Mode threshold: ${draft.toInt()}%", fontWeight = FontWeight.Bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TvSecondaryButton(
                onClick = { commit(draft.toInt() - STEP) },
                modifier = Modifier.size(56.dp).tvFocusScale(cornerRadius = 16.dp),
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Lower threshold")
            }
            Slider(
                value = draft,
                onValueChange = { draft = it },
                onValueChangeFinished = { commit(draft.toInt()) },
                valueRange = min.toFloat()..max.toFloat(),
                steps = max - min - 1,
                modifier = Modifier.weight(1f),
            )
            TvSecondaryButton(
                onClick = { commit(draft.toInt() + STEP) },
                modifier = Modifier.size(56.dp).tvFocusScale(cornerRadius = 16.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Raise threshold")
            }
        }
        Text(
            "Lower percentages mean a calmer timer; 5% is the default.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private const val STEP = 5
