package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.domain.model.ClockTime
import com.ben0551.visualtimeassistant.domain.model.TimerLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** "Set a timer for 8:45" arithmetic, checked against fixed instants rather than the clock. */
class ClockTimeTest {

    private fun now(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Long =
        Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, second)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `later time today gives the remaining seconds`() {
        val at = now(2026, 11, 25, 8, 30, 0)
        assertEquals(15 * 60, ClockTime.secondsUntil("8:45", at))
    }

    @Test
    fun `zero padded input works too`() {
        val at = now(2026, 11, 25, 8, 30, 0)
        assertEquals(15 * 60, ClockTime.secondsUntil("08:45", at))
    }

    @Test
    fun `time already passed rolls over to tomorrow`() {
        val at = now(2026, 11, 25, 9, 0, 0)
        val expected = TimeUnit.MILLISECONDS.toSeconds(
            now(2026, 11, 26, 8, 45, 0) - at,
        ).toInt()
        assertEquals(expected, ClockTime.secondsUntil("8:45", at))
    }

    @Test
    fun `target inside the next ten seconds waits for tomorrow`() {
        val at = now(2026, 11, 25, 8, 44, 55)
        // Rolled forward to tomorrow 8:45 - about 24 h away, so it lands on the 24 h cap.
        assertEquals(TimerLimits.MAX_SECONDS, ClockTime.secondsUntil("8:45", at))
    }

    @Test
    fun `malformed or impossible times are rejected`() {
        val at = now(2026, 11, 25, 8, 30, 0)
        assertNull(ClockTime.secondsUntil("25:00", at))
        assertNull(ClockTime.secondsUntil("8:60", at))
        assertNull(ClockTime.secondsUntil("abc", at))
        assertNull(ClockTime.secondsUntil("15", at)) // bare numbers stay minutes, not clocks
    }
}
