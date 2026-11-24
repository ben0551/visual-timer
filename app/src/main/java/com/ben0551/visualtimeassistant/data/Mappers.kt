package com.ben0551.visualtimeassistant.data

import com.ben0551.visualtimeassistant.data.entity.AppSettingsEntity
import com.ben0551.visualtimeassistant.data.entity.PresetEntity
import com.ben0551.visualtimeassistant.data.entity.TimerSessionEntity
import com.ben0551.visualtimeassistant.domain.model.AppSettings
import com.ben0551.visualtimeassistant.domain.model.DisplayMode
import com.ben0551.visualtimeassistant.domain.model.Preset
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerSessionRecord
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId

fun PresetEntity.toDomain(): Preset = Preset(
    id = id,
    name = name,
    durationSec = durationSec,
    theme = ThemeId.fromName(theme),
    warningProfile = WarningProfileId.fromName(warningProfileId),
    isFavourite = isFavourite,
    sortOrder = sortOrder,
)

fun Preset.toEntity(sortOrder: Int = this.sortOrder): PresetEntity = PresetEntity(
    id = id,
    name = name.trim(),
    durationSec = durationSec,
    theme = theme.name,
    warningProfileId = warningProfile.name,
    isFavourite = isFavourite,
    sortOrder = sortOrder,
)

fun TimerSessionEntity.toDomain(): TimerSessionRecord = TimerSessionRecord(
    id = id,
    presetId = presetId,
    presetName = presetName,
    startTime = startTime,
    endTime = endTime,
    plannedDurationSec = plannedDurationSec,
    actualDurationSec = actualDurationSec,
    overtimeSec = overtimeSec,
    completedOnTime = completedOnTime,
    themeUsed = ThemeId.fromName(themeUsed),
    warningProfileId = WarningProfileId.fromName(warningProfileId),
    displayMode = DisplayMode.fromName(displayMode),
)

fun TimerSessionRecord.toEntity(): TimerSessionEntity = TimerSessionEntity(
    id = id,
    presetId = presetId,
    presetName = presetName,
    startTime = startTime,
    endTime = endTime,
    plannedDurationSec = plannedDurationSec,
    actualDurationSec = actualDurationSec,
    overtimeSec = overtimeSec,
    completedOnTime = completedOnTime,
    themeUsed = themeUsed.name,
    warningProfileId = warningProfileId.name,
    displayMode = displayMode.name,
)

fun AppSettingsEntity.toDomain(): AppSettings = AppSettings(
    showCountdown = showCountdown,
    showPercentage = showPercentage,
    showProgressBar = showProgressBar,
    displayMode = DisplayMode.fromName(displayMode),
    theme = ThemeId.fromName(theme),
    voiceEnabled = voiceEnabled,
    customMilestones = parseMilestones(customMilestones),
    dadModeEnabled = dadModeEnabled,
    dadModeThresholdPercent = dadModeThresholdPercent,
    highContrast = highContrast,
    largeText = largeText,
    reducedMotion = reducedMotion,
    colourBlindPalette = colourBlindPalette,
    muteMode = muteMode,
    overtimeEnabled = overtimeEnabled,
)

fun AppSettings.toEntity(): AppSettingsEntity = AppSettingsEntity(
    id = AppSettingsEntity.ROW_ID,
    showCountdown = showCountdown,
    showPercentage = showPercentage,
    showProgressBar = showProgressBar,
    displayMode = displayMode.name,
    theme = theme.name,
    voiceEnabled = voiceEnabled,
    customMilestones = milestonesToCsv(customMilestones),
    dadModeEnabled = dadModeEnabled,
    dadModeThresholdPercent = dadModeThresholdPercent,
    highContrast = highContrast,
    largeText = largeText,
    reducedMotion = reducedMotion,
    colourBlindPalette = colourBlindPalette,
    muteMode = muteMode,
    overtimeEnabled = overtimeEnabled,
)

/** Parse "25,50,75" into [25, 50, 75]; drops junk and clamps to 1..100. */
fun parseMilestones(csv: String?): List<Int> =
    csv?.split(',', ' ')
        ?.mapNotNull { it.trim().toIntOrNull() }
        ?.filter { it in 1..100 }
        ?.distinct()
        ?.sorted()
        ?: emptyList()

fun milestonesToCsv(milestones: List<Int>): String =
    milestones.filter { it in 1..100 }.distinct().sorted().joinToString(",")
