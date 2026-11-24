package com.ben0551.visualtimeassistant.domain.model

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * "Set a timer for 8:45" - a wall-clock target instead of a duration.
 *
 * The app computes how long it is from now until that time (today, or tomorrow when the
 * time has already passed) and starts a normal countdown with the title "8:45 Timer".
 * Keeping the arithmetic here makes it testable without waiting on the device clock.
 */
object ClockTime {

    private val PATTERN = Regex("""^\s*(\d{1,2})[:.](\d{1,2})\s*$""")

    /** hour to minute to second-of-day when the target next occurs, else null when malformed. */
    fun nextOccurrence(clock: String, nowMillis: Long): Long? {
        val match = PATTERN.find(clock) ?: return null
        val hour = match.groupValues[1].toIntOrNull() ?: return null
        val minute = match.groupValues[2].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null

        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // A target within ten seconds of "now" is already gone; wait for the next one.
        if (target.timeInMillis - nowMillis <= TimerLimits.MIN_SECONDS * 1000L) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    /**
     * Seconds from [nowMillis] until [clock], clamped to the timer's valid range.
     * Returns null when the input is not a clock time or lands outside 10 s .. 24 h.
     */
    fun secondsUntil(clock: String, nowMillis: Long): Int? {
        val target = nextOccurrence(clock, nowMillis) ?: return null
        val raw = TimeUnit.MILLISECONDS.toSeconds(target - nowMillis).toInt()
        if (raw < TimerLimits.MIN_SECONDS) return null
        return raw.coerceAtMost(TimerLimits.MAX_SECONDS)
    }
}
