package com.ben0551.visualtimeassistant.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ben0551.visualtimeassistant.data.entity.AppSettingsEntity
import com.ben0551.visualtimeassistant.data.entity.PresetEntity
import com.ben0551.visualtimeassistant.data.entity.TimerSessionEntity

@Database(
    entities = [
        TimerSessionEntity::class,
        PresetEntity::class,
        AppSettingsEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun presetDao(): PresetDao
    abstract fun settingsDao(): SettingsDao
}
