package com.ben0551.visualtimeassistant.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row settings table (id is always [ROW_ID]).
 * Stored in Room (not DataStore) so the whole app has one persistence stack.
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = ROW_ID,
    val showCountdown: Boolean = true,
    val showPercentage: Boolean = true,
    val showProgressBar: Boolean = true,
    val displayMode: String = "BAR",
    val theme: String = "FOREST",
    val voiceEnabled: Boolean = true,
    /** Comma separated percentages, e.g. "25,50,75,100". */
    val customMilestones: String = "25,50,75,100",
    val dadModeEnabled: Boolean = true,
    val dadModeThresholdPercent: Int = 5,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reducedMotion: Boolean = false,
    val colourBlindPalette: Boolean = false,
    val muteMode: Boolean = false,
    /** When false, reaching zero ends the timer: no overtime clock and no "late" tracking. */
    val overtimeEnabled: Boolean = true,
) {
    companion object {
        const val ROW_ID = 1
    }
}
