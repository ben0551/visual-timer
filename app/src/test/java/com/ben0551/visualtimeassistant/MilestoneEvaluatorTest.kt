package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.domain.model.VoiceMessages
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import com.ben0551.visualtimeassistant.timer.AlertKind
import com.ben0551.visualtimeassistant.timer.MilestoneEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MilestoneEvaluatorTest {

    private val totalSeconds = 1000

    private fun evaluator(
        dadEnabled: Boolean = true,
        custom: List<Int> = listOf(40, 80),
    ) = MilestoneEvaluator(
        totalSeconds = totalSeconds,
        profile = WarningProfileId.DEFAULT,
        dadModeEnabled = dadEnabled,
        dadThresholdPercent = 5,
        customMilestones = custom,
    )

    @Test
    fun `fires each milestone exactly once as time runs down`() {
        val engine = evaluator()
        val alerts = mutableListOf<Pair<AlertKind, Int>>()

        listOf(
            1000 to 810, // crosses 80% (custom)
            810 to 510, // nothing
            510 to 490, // chime at 50%
            490 to 400, // crosses 40% (custom)
            400 to 260, // nothing
            260 to 240, // voice at 25%
            240 to 110, // nothing
            110 to 90,  // highlight at 10%
            90 to 60,   // nothing
            60 to 40,   // dad mode at 5%
            40 to 0,    // expiry
            0 to -10,   // overtime: must not refire
        ).forEach { (previous, current) ->
            engine.onTick(previous, current).forEach { alerts += it.kind to it.percent }
        }

        assertEquals(1, alerts.count { it.first == AlertKind.CHIME })
        assertEquals(3, alerts.count { it.first == AlertKind.VOICE })
        assertEquals(1, alerts.count { it.first == AlertKind.VOICE_HIGHLIGHT })
        assertEquals(1, alerts.count { it.first == AlertKind.DAD_MODE })
        assertEquals(1, alerts.count { it.first == AlertKind.EXPIRED })

        assertTrue(engine.onTick(-10, -20).isEmpty())
    }

    @Test
    fun `voice messages match the PRD wording`() {
        assertEquals("Half your time remains.", VoiceMessages.remaining(50))
        assertEquals("One quarter of your time remains.", VoiceMessages.remaining(25))
        assertEquals("Only ten percent remaining.", VoiceMessages.remaining(10))
        assertEquals("Only 80 percent remaining.", VoiceMessages.remaining(80))
    }

    @Test
    fun `dad mode honours the enabled flag`() {
        val engine = evaluator(dadEnabled = false)
        val alerts = engine.onTick(60, 40)
        assertTrue(alerts.none { it.kind == AlertKind.DAD_MODE })
    }

    @Test
    fun `dad mode message says how long is actually left`() {
        val fortyFiveSeconds = VoiceMessages.dadMode(thresholdPercent = 5, totalSeconds = 900)
        assertTrue(fortyFiveSeconds.startsWith("45 seconds remaining."))
        val fiveMinutes = VoiceMessages.dadMode(thresholdPercent = 25, totalSeconds = 1200)
        assertTrue(fiveMinutes.startsWith("5 minutes remaining."))
        assertEquals("⚠ 45 SECONDS LEFT ⚠", VoiceMessages.dadModeBanner(5, 900))
    }

    @Test
    fun `strict profile announces more often`() {
        val strict = MilestoneEvaluator(
            totalSeconds = totalSeconds,
            profile = WarningProfileId.STRICT,
            dadModeEnabled = false,
            dadThresholdPercent = 5,
        )
        val alerts = mutableListOf<AlertKind>()
        listOf(1000 to 390, 390 to 240, 240 to 90, 90 to 0).forEach { (prev, cur) ->
            strict.onTick(prev, cur).forEach { alerts += it.kind }
        }
        // 40% chime + 50/25/10% voices + expiry
        assertEquals(1, alerts.count { it == AlertKind.CHIME })
        assertEquals(3, alerts.count { it == AlertKind.VOICE || it == AlertKind.VOICE_HIGHLIGHT })
        assertEquals(1, alerts.count { it == AlertKind.EXPIRED })
    }
}
