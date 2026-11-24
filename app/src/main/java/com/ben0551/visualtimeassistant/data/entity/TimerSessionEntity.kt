package com.ben0551.visualtimeassistant.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timer_sessions",
    indices = [Index("startTime"), Index("presetName")],
)
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val presetId: Long? = null,
    val presetName: String,
    /** Epoch millis when the timer was started. */
    val startTime: Long,
    /** Epoch millis when the parent pressed DONE, or null if not finished. */
    val endTime: Long? = null,
    val plannedDurationSec: Int,
    val actualDurationSec: Int,
    val overtimeSec: Int,
    val completedOnTime: Boolean,
    val themeUsed: String,
    val warningProfileId: String,
    val displayMode: String,
)
