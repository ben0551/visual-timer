package com.ben0551.visualtimeassistant.ui.stats

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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ben0551.visualtimeassistant.domain.model.TaskStat
import com.ben0551.visualtimeassistant.util.DurationFormatter

/** Parent dashboard: how long tasks actually take versus what was planned. */
@Composable
fun StatsScreen() {
    val viewModel: StatsViewModel = hiltViewModel()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    "Statistics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Real timings from your family's timers.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (!summary.hasData) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        "No timer data yet. Start a timer, press DONE when the task is finished, and the numbers will appear here.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        if (summary.hasData) {
            item {
                SectionTitle("TODAY")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBlock("TIMERS RUN", summary.todayRun.toString(), Modifier.weight(1f))
                    StatBlock("ON TIME", summary.todayOnTime.toString(), Modifier.weight(1f))
                    StatBlock("LATE", summary.todayLate.toString(), Modifier.weight(1f))
                }
            }

            item {
                SectionTitle("AVERAGE OVERTIME")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBlock(
                        "LAST 7 DAYS",
                        summary.last7DaysAvgOvertimeSec?.let(DurationFormatter::short) ?: "—",
                        Modifier.weight(1f),
                    )
                    StatBlock(
                        "LAST 30 DAYS",
                        summary.last30DaysAvgOvertimeSec?.let(DurationFormatter::short) ?: "—",
                        Modifier.weight(1f),
                    )
                }
            }

            if (summary.difficultTasks.isNotEmpty()) {
                item {
                    SectionTitle("MOST DIFFICULT TASKS")
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.difficultTasks.forEach { TaskRow(it) }
                    }
                }
            }

            if (summary.easyTasks.isNotEmpty()) {
                item {
                    SectionTitle("EASIEST TASKS")
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        summary.easyTasks.forEach { TaskRow(it) }
                    }
                }
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
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TaskRow(stat: TaskStat) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stat.presetName, fontWeight = FontWeight.Bold)
                Text(
                    "${stat.runs} runs · planned ${DurationFormatter.clock(stat.plannedAvgSec)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                DurationFormatter.short(stat.overtimeAvgSec),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (stat.overtimeAvgSec > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
