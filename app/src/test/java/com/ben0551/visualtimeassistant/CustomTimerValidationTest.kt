package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.domain.model.StatusText
import com.ben0551.visualtimeassistant.domain.model.TimerLimits
import com.ben0551.visualtimeassistant.ui.custom.CustomTimerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomTimerValidationTest {

    @Test
    fun `minimum is ten seconds`() {
        val tooShort = CustomTimerUiState(minutes = 0, seconds = 9)
        assertNotNull(tooShort.validationError)
        assertFalse(tooShort.isValid)

        val minimum = CustomTimerUiState(minutes = 0, seconds = 10)
        assertNull(minimum.validationError)
        assertTrue(minimum.isValid)
    }

    @Test
    fun `maximum is twenty four hours`() {
        val maximum = CustomTimerUiState(hours = 24, minutes = 0, seconds = 0)
        assertEquals(TimerLimits.MAX_SECONDS, maximum.totalSeconds)
        assertTrue(maximum.isValid)

        val over = CustomTimerUiState(hours = 24, minutes = 0, seconds = 1)
        assertNotNull(over.validationError)
    }

    @Test
    fun `total combines units`() {
        val state = CustomTimerUiState(hours = 1, minutes = 30, seconds = 30)
        assertEquals(5430, state.totalSeconds)
    }

    @Test
    fun `status text reflects progress`() {
        assertEquals(StatusText.GETTING_STARTED, StatusText.of(0.1f, 900, 0))
        assertEquals(StatusText.HALF_WAY, StatusText.of(0.6f, 360, 0))
        assertEquals("2 MINUTES LEFT", StatusText.of(0.9f, 120, 0))
        assertEquals(StatusText.TIME_IS_UP, StatusText.of(1f, -34, 34))
    }
}
