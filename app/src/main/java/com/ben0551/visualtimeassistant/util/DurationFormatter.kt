package com.ben0551.visualtimeassistant.util

import kotlin.math.abs
import kotlin.math.ceil

object DurationFormatter {

    /** "9:59" -> "09:59"; "3725" -> "1:02:05". Negative values clamp to zero. */
    fun clock(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
    }

    /** Signed short form for analytics: +240 -> "+4m 00s", -32 -> "-32s". */
    fun short(signedSeconds: Int): String {
        val sign = if (signedSeconds >= 0) "+" else "-"
        val absVal = abs(signedSeconds)
        val m = absVal / 60
        val s = absVal % 60
        return if (m > 0) "$sign${m}m ${"%02d".format(s)}s" else "$sign${s}s"
    }

    /** Whole minutes remaining, rounded up ("14:32" -> "15 minutes"). */
    fun minutesRoundedUp(totalSeconds: Int): Int = ceil(totalSeconds / 60.0).toInt()

    /** Card label, e.g. "5 MINUTES", "1 HOUR 30 MINUTES", "2 MINUTES". */
    fun words(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        val hours = s / 3600
        val minutes = (s % 3600) / 60
        return when {
            hours > 0 && minutes > 0 ->
                "$hours HOUR${if (hours > 1) "S" else ""} $minutes MIN${if (minutes > 1) "UTES" else "UTE"}"
            hours > 0 -> "$hours HOUR${if (hours > 1) "S" else ""}"
            else -> "$minutes MINUTE${if (minutes != 1) "S" else ""}"
        }
    }

    /** Percent of [totalSeconds] that [remainingSeconds] represents, rounded to nearest int. */
    fun remainingPercent(totalSeconds: Int, remainingSeconds: Int): Int =
        if (totalSeconds <= 0) 0
        else (((remainingSeconds.toDouble() / totalSeconds) * 100).toInt().coerceIn(0, 100))
}
