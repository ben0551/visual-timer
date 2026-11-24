package com.ben0551.visualtimeassistant.domain.model

import kotlin.math.ceil

/** Spoken messages used by the warning system and dad mode. */
object VoiceMessages {

    /** Announcement for a remaining-percent milestone. */
    fun remaining(percent: Int): String = when (percent) {
        50 -> "Half your time remains."
        25 -> "One quarter of your time remains."
        10 -> "Only ten percent remaining."
        100 -> "Your time starts now."
        else -> "Only $percent percent remaining."
    }

    /** Dad Mode line. [thresholdPercent] of [totalSeconds] translates to whole minutes. */
    fun dadMode(thresholdPercent: Int, totalSeconds: Int): String {
        val minutes = ceil((totalSeconds * thresholdPercent / 100.0) / 60.0).toInt().coerceAtLeast(1)
        val label = if (minutes == 1) "One minute" else "$minutes minutes"
        return "$label remaining. Finish what you're doing."
    }

    /** Dad Mode banner text, e.g. "ONE MINUTE LEFT". */
    fun dadModeBanner(thresholdPercent: Int, totalSeconds: Int): String {
        val minutes = ceil((totalSeconds * thresholdPercent / 100.0) / 60.0).toInt().coerceAtLeast(1)
        val label = if (minutes == 1) "ONE MINUTE" else "$minutes MINUTES"
        return "⚠ $label LEFT ⚠"
    }

    const val TIME_IS_UP = "Time is up."
}

/** Optional status line under the countdown. */
object StatusText {
    const val GETTING_STARTED = "GETTING STARTED"
    const val KEEP_GOING = "KEEP GOING"
    const val HALF_WAY = "HALF WAY"
    const val TIME_IS_UP = "TIME IS UP"

    fun of(elapsedFraction: Float, remainingSec: Int, overtimeSec: Int): String {
        if (overtimeSec > 0) return TIME_IS_UP
        return when {
            elapsedFraction < 0.25f -> GETTING_STARTED
            elapsedFraction < 0.55f -> KEEP_GOING
            elapsedFraction < 0.85f -> HALF_WAY
            remainingSec > 60 -> "${remainingSec / 60} MINUTES LEFT"
            else -> "LESS THAN A MINUTE"
        }
    }
}
