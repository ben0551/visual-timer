package com.ben0551.visualtimeassistant.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    /** Duration in seconds. */
    val durationSec: Int,
    val theme: String,
    val warningProfileId: String,
    val isFavourite: Boolean = false,
    val sortOrder: Int = 0,
)
