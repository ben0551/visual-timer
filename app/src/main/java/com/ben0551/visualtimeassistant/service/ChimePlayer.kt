package com.ben0551.visualtimeassistant.service

import android.media.AudioManager
import android.media.ToneGenerator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Short non-speech audio cues via ToneGenerator, so the app ships with zero audio assets.
 * Chime = milestone reached, Completion = timer expired / dad mode urgency.
 */
@Singleton
class ChimePlayer @Inject constructor() {

    private var toneGenerator: ToneGenerator? = null

    @Synchronized
    fun chime() = play(ToneGenerator.TONE_PROP_BEEP, 180)

    @Synchronized
    fun completion() = play(ToneGenerator.TONE_PROP_BEEP2, 400)

    @Synchronized
    private fun play(toneType: Int, durationMs: Int) {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, VOLUME)
            }
            toneGenerator?.startTone(toneType, durationMs)
        } catch (_: Exception) {
            // Audio focus / hardware issues must never crash the timer.
        }
    }

    private companion object {
        const val VOLUME = 80
    }
}
