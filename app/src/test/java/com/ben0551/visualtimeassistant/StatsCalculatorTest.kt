package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.data.parseMilestones
import com.ben0551.visualtimeassistant.domain.model.DisplayMode
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerSessionRecord
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import com.ben0551.visualtimeassistant.domain.stats.StatsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class StatsCalculatorTest {

    private fun session(
        presetName: String,
        planned: Int,
        actual: Int,
        startTime: Long,
    ) = TimerSessionRecord(
        presetName = presetName,
        startTime = startTime,
        endTime = startTime + actual * 1000L,
        plannedDurationSec = planned,
        actualDurationSec = actual,
        overtimeSec = actual - planned,
        completedOnTime = actual <= planned,
        themeUsed = ThemeId.FOREST,
        warningProfileId = WarningProfileId.DEFAULT,
        displayMode = DisplayMode.BAR,
    )

    @Test
    fun `aggregates today windows and task difficulty`() {
        val now = dayAt(10, 0)
        val sessions = listOf(
            session("Morning Routine", planned = 900, actual = 1140, startTime = now - 3_600_000L),
            session("Pack School Bag", planned = 300, actual = 268, startTime = now - 2 * 3_600_000L),
            session("Morning Routine", planned = 900, actual = 1000, startTime = now - 8L * 24 * 3_600_000L),
        )

        val summary = StatsCalculator.compute(sessions, now)

        assertEquals(2, summary.todayRun)
        assertEquals(1, summary.todayOnTime)
        assertEquals(1, summary.todayLate)
        assertEquals(104, summary.last7DaysAvgOvertimeSec) // (240 - 32) / 2
        assertEquals(103, summary.last30DaysAvgOvertimeSec) // (240 - 32 + 100) / 3

        assertEquals(1, summary.difficultTasks.size)
        assertEquals("Morning Routine", summary.difficultTasks.first().presetName)
        assertEquals(170, summary.difficultTasks.first().overtimeAvgSec)

        assertEquals(1, summary.easyTasks.size)
        assertEquals("Pack School Bag", summary.easyTasks.first().presetName)
        assertEquals(-32, summary.easyTasks.first().overtimeAvgSec)
    }

    @Test
    fun `empty history yields empty summary`() {
        val summary = StatsCalculator.compute(emptyList(), System.currentTimeMillis())
        assertTrue(!summary.hasData)
        assertNull(summary.last7DaysAvgOvertimeSec)
    }

    @Test
    fun `milestone parsing drops junk and clamps`() {
        assertEquals(listOf(20, 40, 60, 80), parseMilestones("80, 20, abc, 40, 0, 60, 20"))
        assertEquals(emptyList<Int>(), parseMilestones(null))
        assertEquals(listOf(25, 50, 75, 100), parseMilestones("25,50,75,100"))
    }

    /** Fixed local-time instant: 15 Jan 2026, 10:00. */
    private fun dayAt(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(2026, Calendar.JANUARY, 15, hour, minute, 0)
        return calendar.timeInMillis
    }
}
