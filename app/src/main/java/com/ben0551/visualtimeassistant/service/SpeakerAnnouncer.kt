package com.ben0551.visualtimeassistant.service

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper around Android's native TextToSpeech engine.
 * Used for all voice announcements ("Half your time remains.", Dad Mode, ...).
 */
@Singleton
class SpeakerAnnouncer @Inject constructor(
    @ApplicationContext context: Context,
) {
    private var tts: TextToSpeech? = null
    private var pending: String? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                runCatching { tts?.language = Locale.getDefault() }
                pending?.let { text -> pending = null; speakInternal(text) }
            }
        }
    }

    /** Speaks immediately, replacing anything already being said. */
    fun speak(text: String) {
        if (text.isBlank()) return
        if (tts == null) {
            pending = text
            return
        }
        speakInternal(text)
    }

    fun stop() {
        pending = null
        runCatching { tts?.stop() }
    }

    private fun speakInternal(text: String) {
        runCatching {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "visual_time_$text")
        }
    }
}
