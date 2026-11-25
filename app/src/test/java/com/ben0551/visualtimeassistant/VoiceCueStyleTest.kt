package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.domain.model.AnnouncementStyle
import com.ben0551.visualtimeassistant.domain.model.VoiceMessages
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import com.ben0551.visualtimeassistant.timer.AlertKind
import com.ben0551.visualtimeassistant.timer.MilestoneEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceCueStyleTest {

    @Test
    fun `time remaining style speaks minutes left instead of a percentage`() {
        assertEquals(
            "10 minutes remaining.",
            VoiceMessages.remaining(percent = 50, remainingSec = 600, style = AnnouncementStyle.TIME_REMAINING),
        )
    }

    @Test
    fun `time remaining style speaks seconds for short timers`() {
        assertEquals(
            "45 seconds remaining.",
            VoiceMessages.remaining(percent = 25, remainingSec = 45, style = AnnouncementStyle.TIME_REMAINING),
        )
    }

    @Test
    fun `evaluator speaks remaining time at milestones when time style is selected`() {
        val evaluator = MilestoneEvaluator(
            totalSeconds = 1200,
            profile = WarningProfileId.STRICT,
            dadModeEnabled = false,
            dadThresholdPercent = 5,
            announcementStyle = AnnouncementStyle.TIME_REMAINING,
        )

        val voice = evaluator.onTick(700, 600).first { it.kind == AlertKind.VOICE }

        assertEquals("10 minutes remaining.", voice.message)
    }
}
