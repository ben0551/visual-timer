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

    /** Announcement in the parent's chosen style, using the actual seconds left. */
    fun remaining(percent: Int, remainingSec: Int, style: AnnouncementStyle): String =
        if (style == AnnouncementStyle.TIME_REMAINING) {
            "${humanDuration(remainingSec.coerceAtLeast(0))} remaining."
        } else {
            remaining(percent)
        }

    /** Dad Mode line: says how much time is actually left at the threshold. */
    fun dadMode(thresholdPercent: Int, totalSeconds: Int): String {
        val remainingSec = remainingSeconds(thresholdPercent, totalSeconds)
        return "${humanDuration(remainingSec)} remaining. Finish what you're doing."
    }

    /** Dad Mode banner text, e.g. "ONE MINUTE LEFT" / "45 SECONDS LEFT". */
    fun dadModeBanner(thresholdPercent: Int, totalSeconds: Int): String {
        val remainingSec = remainingSeconds(thresholdPercent, totalSeconds)
        return "⚠ ${humanDuration(remainingSec).uppercase()} LEFT ⚠"
    }

    private fun remainingSeconds(thresholdPercent: Int, totalSeconds: Int): Int =
        Math.round(totalSeconds * thresholdPercent / 100.0f).toInt().coerceAtLeast(1)

    /** "45 seconds", "1 minute", "2 minutes". */
    private fun humanDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        return if (minutes >= 1) {
            if (minutes == 1) "1 minute" else "$minutes minutes"
        } else {
            "$totalSeconds seconds"
        }
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
            remainingSec > 120 -> "${remainingSec / 60} MINUTES LEFT"
            remainingSec >= 60 -> "1 MINUTE LEFT"
            else -> "$remainingSec SECONDS LEFT"
        }
    }
}
