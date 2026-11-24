package com.ben0551.visualtimeassistant.data.repo

import com.ben0551.visualtimeassistant.data.db.PresetDao
import com.ben0551.visualtimeassistant.data.toDomain
import com.ben0551.visualtimeassistant.data.toEntity
import com.ben0551.visualtimeassistant.domain.model.Preset
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetRepository @Inject constructor(
    private val presetDao: PresetDao,
) {
    val presets: Flow<List<Preset>> =
        presetDao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Seeds the starter presets on first launch only. */
    suspend fun ensureSeeded() {
        if (presetDao.count() > 0) return
        DEFAULT_PRESETS.forEachIndexed { index, preset ->
            presetDao.insert(preset.toEntity(sortOrder = index))
        }
    }

    suspend fun byId(id: Long): Preset? = presetDao.byId(id)?.toDomain()

    suspend fun save(preset: Preset) {
        if (preset.id == 0L) {
            presetDao.insert(preset.toEntity())
        } else {
            presetDao.update(preset.toEntity())
        }
    }

    suspend fun delete(preset: Preset) = presetDao.delete(preset.toEntity())

    suspend fun toggleFavourite(preset: Preset) =
        presetDao.setFavourite(preset.id, !preset.isFavourite)

    companion object {
        private val DEFAULT_PRESETS = listOf(
            Preset(name = "Morning Routine", durationSec = 15 * 60, theme = ThemeId.FOREST),
            Preset(name = "Shoes On", durationSec = 2 * 60, theme = ThemeId.POTATO),
            Preset(name = "Brush Teeth", durationSec = 2 * 60, theme = ThemeId.AQUARIUM),
            Preset(name = "Pack School Bag", durationSec = 5 * 60, theme = ThemeId.ROCKET),
            Preset(name = "Screen Time", durationSec = 20 * 60, theme = ThemeId.SPACE),
            Preset(name = "Bed Time", durationSec = 30 * 60, theme = ThemeId.SPACE),
        )
    }
}
