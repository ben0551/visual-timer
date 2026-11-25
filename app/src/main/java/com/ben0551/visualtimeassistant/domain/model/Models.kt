package com.ben0551.visualtimeassistant.domain.model

/** Parent-editable named timer. */
data class Preset(
    val id: Long = 0L,
    val name: String,
    val durationSec: Int,
    val theme: ThemeId = ThemeId.FOREST,
    val warningProfile: WarningProfileId = WarningProfileId.DEFAULT,
    val isFavourite: Boolean = false,
    val sortOrder: Int = 0,
)

/** Single source of truth for all user preferences. */
data class AppSettings(
    val showCountdown: Boolean = true,
    val showPercentage: Boolean = true,
    val showProgressBar: Boolean = true,
    val displayMode: DisplayMode = DisplayMode.BAR,
    val theme: ThemeId = ThemeId.FOREST,
    val voiceEnabled: Boolean = true,
    val announcementStyle: AnnouncementStyle = AnnouncementStyle.PERCENT,
    val customMilestones: List<Int> = listOf(25, 50, 75, 100),
    val dadModeEnabled: Boolean = true,
    val dadModeThresholdPercent: Int = TimerLimits.DAD_MODE_DEFAULT_THRESHOLD_PERCENT,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reducedMotion: Boolean = false,
    val colourBlindPalette: Boolean = false,
    val muteMode: Boolean = false,
    /** Master switch for overtime: when off, "TIME IS UP" stops the timer where it is. */
    val overtimeEnabled: Boolean = false,
)

/** One completed (or abandoned-but-expired) timer run. */
data class TimerSessionRecord(
    val id: Long = 0L,
    val presetId: Long? = null,
    val presetName: String,
    val startTime: Long,
    val endTime: Long?,
    val plannedDurationSec: Int,
    val actualDurationSec: Int,
    val overtimeSec: Int,
    val completedOnTime: Boolean,
    val themeUsed: ThemeId,
    val warningProfileId: WarningProfileId,
    val displayMode: DisplayMode,
)

/** Per-task aggregate shown on the statistics dashboard. */
data class TaskStat(
    val presetName: String,
    val runs: Int,
    val plannedAvgSec: Int,
    val actualAvgSec: Int,
    val overtimeAvgSec: Int,
)

data class StatsSummary(
    val todayRun: Int = 0,
    val todayOnTime: Int = 0,
    val todayLate: Int = 0,
    val last7DaysAvgOvertimeSec: Int? = null,
    val last30DaysAvgOvertimeSec: Int? = null,
    val difficultTasks: List<TaskStat> = emptyList(),
    val easyTasks: List<TaskStat> = emptyList(),
) {
    val hasData: Boolean get() = todayRun > 0 || last30DaysAvgOvertimeSec != null
}
