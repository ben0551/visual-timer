package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.util.DurationFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatterTest {

    @Test
    fun `clock formats minutes and hours`() {
        assertEquals("00:10", DurationFormatter.clock(10))
        assertEquals("14:32", DurationFormatter.clock(872))
        assertEquals("1:02:05", DurationFormatter.clock(3725))
        assertEquals("00:00", DurationFormatter.clock(-5))
    }

    @Test
    fun `short form keeps the sign for analytics`() {
        assertEquals("+4m 00s", DurationFormatter.short(240))
        assertEquals("-32s", DurationFormatter.short(-32))
        assertEquals("+0s", DurationFormatter.short(0))
    }

    @Test
    fun `words describe durations for cards`() {
        assertEquals("5 MINUTES", DurationFormatter.words(300))
        assertEquals("1 HOUR 30 MINUTES", DurationFormatter.words(5400))
        assertEquals("1 MINUTE", DurationFormatter.words(60))
    }

    @Test
    fun `minutes rounded up`() {
        assertEquals(15, DurationFormatter.minutesRoundedUp(872))
        assertEquals(1, DurationFormatter.minutesRoundedUp(1))
    }

    @Test
    fun `remaining percent clamps`() {
        assertEquals(50, DurationFormatter.remainingPercent(900, 450))
        assertEquals(100, DurationFormatter.remainingPercent(900, 900))
        assertEquals(0, DurationFormatter.remainingPercent(900, -30))
    }
}
