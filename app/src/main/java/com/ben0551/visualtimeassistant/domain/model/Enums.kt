package com.ben0551.visualtimeassistant.domain.model

/** Visual themes available for the running timer. */
enum class ThemeId(val displayName: String) {
    FOREST("Forest Growth"),
    ROCKET("Rocket Launch"),
    AQUARIUM("Aquarium"),
    SPACE("Space Journey"),
    POTATO("Potato Empire™"),
    /** Selection-only option in settings; resolved to a concrete theme before the run starts. */
    RANDOM("Random");

    /** Concrete theme to render; RANDOM picks one at random. */
    fun resolve(): ThemeId = if (this == RANDOM) random() else this

    companion object {
        fun fromName(name: String?): ThemeId =
            entries.firstOrNull { it.name == name && it != RANDOM } ?: FOREST

        fun selectable(): List<ThemeId> = entries.toList()

        fun random(): ThemeId = listOf(FOREST, ROCKET, AQUARIUM, SPACE, POTATO).random()
    }
}

/** How remaining/elapsed time is visualised on the running screen. */
enum class DisplayMode(val label: String) {
    BAR("Bar"),
    CIRCULAR("Circular"),
    BLOCK("Blocks"),
    COMBINED("Combined");

    companion object {
        fun fromName(name: String?): DisplayMode = entries.firstOrNull { it.name == name } ?: BAR
    }
}

/** How milestone announcements describe the time left. */
enum class AnnouncementStyle(val label: String) {
    PERCENT("Percentage"),
    TIME_REMAINING("Time left");

    companion object {
        fun fromName(name: String?): AnnouncementStyle =
            entries.firstOrNull { it.name == name } ?: PERCENT
    }
}

/**
 * Parent-configurable urgency profile.
 * [chimePercent] fires an audible chime when this much time is left,
 * [voicePercents] fire spoken announcements.
 */
enum class WarningProfileId(
    val label: String,
    val chimePercent: Int,
    val voicePercents: List<Int>,
) {
    DEFAULT("Standard", 50, listOf(25, 10)),
    GENTLE("Gentle", 60, listOf(30)),
    STRICT("Strict", 40, listOf(50, 25, 10));

    companion object {
        fun fromName(name: String?): WarningProfileId =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

object TimerLimits {
    const val MIN_SECONDS = 10
    const val MAX_SECONDS = 24 * 60 * 60

    /**
     * Below this many seconds remaining the warning system stays quiet: countdown chatter in
     * the last few seconds of a short timer arrives on top of itself and means nothing to a
     * child ("1 second remaining" right before "time up"). Expiry always announces.
     */
    const val MIN_ANNOUNCE_SECONDS = 15
    const val DAD_MODE_DEFAULT_THRESHOLD_PERCENT = 5
    const val DAD_MODE_MIN_THRESHOLD_PERCENT = 1
    const val DAD_MODE_MAX_THRESHOLD_PERCENT = 50
}
