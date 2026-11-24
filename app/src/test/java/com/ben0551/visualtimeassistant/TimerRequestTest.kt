package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerLimits
import com.ben0551.visualtimeassistant.domain.model.TimerRequest
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Hermes deep-link contract: whatever arrives via visualtime://timer must map onto a
 * valid [TimerRequest]. Uri parsing itself is exercised on-device by firing the real intent.
 */
class TimerRequestTest {

    @Test
    fun `query parameters become a request`() {
        val request = TimerRequest.fromQueryParameters(
            seconds = 900,
            name = "Morning Routine",
            theme = "POTATO",
            profile = "GENTLE",
        )

        assertEquals(900, request.seconds)
        assertEquals("Morning Routine", request.presetName)
        assertEquals(ThemeId.POTATO, request.theme)
        assertEquals(WarningProfileId.GENTLE, request.warningProfile)
        assertTrue(request.isValid)
    }

    @Test
    fun `missing values fall back to safe defaults`() {
        val request = TimerRequest.fromQueryParameters(null, null, null, null)

        assertEquals(TimerLimits.MIN_SECONDS, request.seconds)
        assertEquals("Custom Timer", request.presetName)
        assertEquals(ThemeId.FOREST, request.theme)
        assertEquals(WarningProfileId.DEFAULT, request.warningProfile)
        assertTrue(request.isValid)
    }

    @Test
    fun `out of range requests are rejected`() {
        assertFalse(TimerRequest(seconds = 5).isValid)
        assertFalse(TimerRequest(seconds = TimerLimits.MAX_SECONDS + 1).isValid)
        assertTrue(TimerRequest(seconds = TimerLimits.MAX_SECONDS).isValid)
    }

    @Test
    fun `random theme resolves to a concrete theme`() {
        val resolved = TimerRequest(seconds = 900, theme = ThemeId.RANDOM).theme.resolve()
        assertTrue(resolved != ThemeId.RANDOM)
    }
}
