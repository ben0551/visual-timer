package com.ben0551.visualtimeassistant.di

import android.content.Context
import androidx.room.Room
import com.ben0551.visualtimeassistant.data.db.AppDatabase
import com.ben0551.visualtimeassistant.data.db.PresetDao
import com.ben0551.visualtimeassistant.data.db.SettingsDao
import com.ben0551.visualtimeassistant.data.db.TimerSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "visual_time_assistant.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTimerSessionDao(database: AppDatabase): TimerSessionDao =
        database.timerSessionDao()

    @Provides
    fun providePresetDao(database: AppDatabase): PresetDao = database.presetDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()
}
