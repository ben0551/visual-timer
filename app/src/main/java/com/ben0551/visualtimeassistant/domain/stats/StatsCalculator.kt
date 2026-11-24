package com.ben0551.visualtimeassistant.domain.stats

import com.ben0551.visualtimeassistant.domain.model.StatsSummary
import com.ben0551.visualtimeassistant.domain.model.TaskStat
import com.ben0551.visualtimeassistant.domain.model.TimerSessionRecord
import java.util.Calendar

/** Pure aggregation of timer sessions into dashboard metrics. */
object StatsCalculator {

    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    fun compute(sessions: List<TimerSessionRecord>, now: Long): StatsSummary {
        if (sessions.isEmpty()) return StatsSummary()

        val startOfDay = startOfDay(now)
        val today = sessions.filter { it.startTime >= startOfDay }
        val last7 = sessions.filter { it.startTime >= now - 7 * DAY_MILLIS }
        val last30 = sessions.filter { it.startTime >= now - 30 * DAY_MILLIS }

        return StatsSummary(
            todayRun = today.size,
            todayOnTime = today.count { it.completedOnTime },
            todayLate = today.count { !it.completedOnTime },
            last7DaysAvgOvertimeSec = averageOvertime(last7),
            last30DaysAvgOvertimeSec = averageOvertime(last30),
            difficultTasks = taskStats(sessions).filter { it.overtimeAvgSec > 0 }
                .sortedByDescending { it.overtimeAvgSec }.take(5),
            easyTasks = taskStats(sessions).filter { it.overtimeAvgSec < 0 }
                .sortedBy { it.overtimeAvgSec }.take(5),
        )
    }

    private fun averageOvertime(sessions: List<TimerSessionRecord>): Int? =
        sessions.takeIf { it.isNotEmpty() }
            ?.let { list -> Math.round(list.sumOf { it.overtimeSec }.toDouble() / list.size).toInt() }

    private fun taskStats(sessions: List<TimerSessionRecord>): List<TaskStat> =
        sessions.groupBy { it.presetName }
            .map { (name, runs) ->
                TaskStat(
                    presetName = name,
                    runs = runs.size,
                    plannedAvgSec = Math.round(runs.sumOf { it.plannedDurationSec }.toDouble() / runs.size).toInt(),
                    actualAvgSec = Math.round(runs.sumOf { it.actualDurationSec }.toDouble() / runs.size).toInt(),
                    overtimeAvgSec = Math.round(runs.sumOf { it.overtimeSec }.toDouble() / runs.size).toInt(),
                )
            }

    private fun startOfDay(now: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
