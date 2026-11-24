package com.ben0551.visualtimeassistant.timer

import com.ben0551.visualtimeassistant.domain.model.VoiceMessages
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId

enum class AlertKind {
    /** Audible chime only. */
    CHIME,

    /** Spoken announcement only. */
    VOICE,

    /** Spoken announcement plus a visual highlight shift. */
    VOICE_HIGHLIGHT,

    /** Dad Mode urgency layer. */
    DAD_MODE,

    /** Timer reached zero; overtime begins. */
    EXPIRED,
}

data class TimerAlert(
    val kind: AlertKind,
    /** Remaining percent the alert was triggered at (0 for expiry). */
    val percent: Int,
    val message: String,
)

/**
 * Stateless-per-session warning engine. Tracks which milestones have already fired so an
 * alert never repeats. Pure Kotlin so it is unit-testable on the JVM.
 */
class MilestoneEvaluator(
    private val totalSeconds: Int,
    private val profile: WarningProfileId,
    private val dadModeEnabled: Boolean,
    private val dadThresholdPercent: Int,
    customMilestones: List<Int> = emptyList(),
) {
    private val fired = HashSet<String>()
    private val custom = customMilestones.filter { it in 1..99 }.distinct().sorted()

    /**
     * Call on every tick with the previous and current remaining seconds.
     * Returns the alerts whose threshold was crossed on this tick.
     */
    fun onTick(previousRemainingSec: Int, remainingSec: Int): List<TimerAlert> {
        if (totalSeconds <= 0) return emptyList()
        val alerts = mutableListOf<TimerAlert>()
        val previousPercent = percentOf(previousRemainingSec)
        val currentPercent = percentOf(remainingSec)

        if (previousRemainingSec > 0 && remainingSec <= 0 && fired.add(EXPIRED_KEY)) {
            alerts += TimerAlert(AlertKind.EXPIRED, 0, VoiceMessages.TIME_IS_UP)
        }

        if (crossed(previousPercent, currentPercent, profile.chimePercent) &&
            fired.add("$CHIME_KEY:${profile.chimePercent}")
        ) {
            alerts += TimerAlert(AlertKind.CHIME, profile.chimePercent, message = "")
        }

        profile.voicePercents.forEach { milestone ->
            if (crossed(previousPercent, currentPercent, milestone) && fired.add("$VOICE_KEY:$milestone")) {
                alerts += TimerAlert(
                    kind = if (milestone <= 10) AlertKind.VOICE_HIGHLIGHT else AlertKind.VOICE,
                    percent = milestone,
                    message = VoiceMessages.remaining(milestone),
                )
            }
        }

        custom.filter { it !in profile.voicePercents }.forEach { milestone ->
            if (crossed(previousPercent, currentPercent, milestone) && fired.add("$CUSTOM_KEY:$milestone")) {
                alerts += TimerAlert(AlertKind.VOICE, milestone, VoiceMessages.remaining(milestone))
            }
        }

        if (dadModeEnabled) {
            val threshold = dadThresholdPercent.coerceIn(1, 99)
            if (crossed(previousPercent, currentPercent, threshold) && fired.add(DAD_KEY)) {
                alerts += TimerAlert(
                    AlertKind.DAD_MODE,
                    threshold,
                    VoiceMessages.dadMode(threshold, totalSeconds),
                )
            }
        }

        return alerts
    }

    private fun percentOf(remainingSec: Int): Int =
        ((remainingSec.toDouble() / totalSeconds) * 100).toInt().coerceIn(0, 100)

    /** Threshold is crossed when we were strictly above it and are now at or below it. */
    private fun crossed(previousPercent: Int, currentPercent: Int, threshold: Int): Boolean =
        previousPercent > threshold && currentPercent <= threshold

    private companion object {
        const val EXPIRED_KEY = "expired"
        const val CHIME_KEY = "chime"
        const val VOICE_KEY = "voice"
        const val CUSTOM_KEY = "custom"
        const val DAD_KEY = "dad"
    }
}
