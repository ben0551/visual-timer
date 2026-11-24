package com.ben0551.visualtimeassistant.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ben0551.visualtimeassistant.data.entity.AppSettingsEntity
import com.ben0551.visualtimeassistant.data.entity.PresetEntity
import com.ben0551.visualtimeassistant.data.entity.TimerSessionEntity

@Database(
    entities = [
        TimerSessionEntity::class,
        PresetEntity::class,
        AppSettingsEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun presetDao(): PresetDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        /** v2 added the overtime kill-switch to the single settings row. */
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN overtimeEnabled INTEGER NOT NULL DEFAULT 1",
                )
            }
        }
    }
}
