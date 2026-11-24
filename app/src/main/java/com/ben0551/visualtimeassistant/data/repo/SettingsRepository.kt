package com.ben0551.visualtimeassistant.data.repo

import com.ben0551.visualtimeassistant.data.db.SettingsDao
import com.ben0551.visualtimeassistant.data.entity.AppSettingsEntity
import com.ben0551.visualtimeassistant.data.toDomain
import com.ben0551.visualtimeassistant.data.toEntity
import com.ben0551.visualtimeassistant.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
) {
    val settings: Flow<AppSettings> =
        settingsDao.observe().map { entity -> entity?.toDomain() ?: AppSettings() }

    suspend fun current(): AppSettings = settingsDao.get()?.toDomain() ?: AppSettings()

    /** Creates the single settings row on first launch. */
    suspend fun ensureRowExists() {
        if (settingsDao.get() == null) {
            settingsDao.upsert(AppSettingsEntity())
        }
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(current())
        settingsDao.upsert(next.toEntity())
    }

    suspend fun resetToDefaults() {
        settingsDao.upsert(AppSettingsEntity())
    }
}
