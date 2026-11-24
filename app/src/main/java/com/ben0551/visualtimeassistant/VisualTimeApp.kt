package com.ben0551.visualtimeassistant

import android.app.Application
import com.ben0551.visualtimeassistant.data.repo.PresetRepository
import com.ben0551.visualtimeassistant.data.repo.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VisualTimeApp : Application() {

    @Inject lateinit var presetRepository: PresetRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Seed starter content off the main thread; the UI observes flows and updates itself.
        appScope.launch {
            runCatching { settingsRepository.ensureRowExists() }
            runCatching { presetRepository.ensureSeeded() }
        }
    }
}
