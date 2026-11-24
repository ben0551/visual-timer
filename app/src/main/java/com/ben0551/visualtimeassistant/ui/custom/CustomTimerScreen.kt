package com.ben0551.visualtimeassistant.ui.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.ui.components.tvFocusScale
import com.ben0551.visualtimeassistant.util.DurationFormatter

/** Screen 2 - build a custom timer (10 seconds to 24 hours). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomTimerScreen(
    onBack: () -> Unit,
    onStart: (seconds: Int, theme: ThemeId) -> Unit,
) {
    val viewModel: CustomTimerViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    "Custom Timer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Set any time from 10 seconds to 24 hours.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            StepperRow(label = "HOURS", value = "%02d".format(state.hours),
                onMinus = { viewModel.stepHours(-1) }, onPlus = { viewModel.stepHours(1) })
        }
        item {
            StepperRow(label = "MINUTES", value = "%02d".format(state.minutes),
                onMinus = { viewModel.stepMinutes(-1) }, onPlus = { viewModel.stepMinutes(1) })
        }
        item {
            StepperRow(label = "SECONDS", value = "%02d".format(state.seconds),
                onMinus = { viewModel.stepSeconds(-5) }, onPlus = { viewModel.stepSeconds(5) })
        }

        item {
            Column {
                Text(
                    "THEME",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ThemeId.selectable().forEach { theme ->
                        FilterChip(
                            selected = state.theme == theme,
                            onClick = { viewModel.setTheme(theme) },
                            label = { Text(theme.displayName) },
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    "QUICK SET",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(1, 2, 5, 10, 15, 30).forEach { minutes ->
                        FilterChip(
                            selected = state.minutes == minutes && state.hours == 0 && state.seconds == 0,
                            onClick = { viewModel.applyQuickMinutes(minutes) },
                            label = { Text("$minutes MIN") },
                        )
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    "TOTAL  ${DurationFormatter.clock(state.totalSeconds)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                val error = state.validationError
                Text(
                    text = error ?: "Looks good.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onStart(state.totalSeconds, state.theme) },
                    enabled = state.isValid,
                    modifier = Modifier.fillMaxWidth().height(64.dp).tvFocusScale(cornerRadius = 20.dp),
                ) {
                    Text("START TIMER", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text("BACK")
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = onMinus,
                modifier = Modifier.size(64.dp).tvFocusScale(cornerRadius = 16.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease $label")
            }
            Spacer(Modifier.width(16.dp))
            Text(value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(16.dp))
            OutlinedButton(
                onClick = onPlus,
                modifier = Modifier.size(64.dp).tvFocusScale(cornerRadius = 16.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase $label")
            }
        }
    }
}
