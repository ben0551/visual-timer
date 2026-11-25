package com.ben0551.visualtimeassistant

import com.ben0551.visualtimeassistant.data.entity.AppSettingsEntity
import com.ben0551.visualtimeassistant.data.toDomain
import com.ben0551.visualtimeassistant.data.toEntity
import com.ben0551.visualtimeassistant.domain.model.AnnouncementStyle
import com.ben0551.visualtimeassistant.domain.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsMappingTest {

    @Test
    fun `announcement style survives the settings round trip`() {
        val settings = AppSettings(announcementStyle = AnnouncementStyle.TIME_REMAINING)

        assertEquals(AnnouncementStyle.TIME_REMAINING, settings.toEntity().toDomain().announcementStyle)
    }

    @Test
    fun `unknown stored style falls back to percentage`() {
        val stored = AppSettingsEntity(announcementStyle = "BOGUS")

        assertEquals(AnnouncementStyle.PERCENT, stored.toDomain().announcementStyle)
    }
}
