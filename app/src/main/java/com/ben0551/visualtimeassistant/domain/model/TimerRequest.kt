package com.ben0551.visualtimeassistant.domain.model

/**
 * External "start this timer now" request, delivered by Hermes (or any automation)
 * through the visualtime:// deep link or intent extras.
 */
data class TimerRequest(
    val seconds: Int,
    val presetName: String = "Custom Timer",
    val theme: ThemeId = ThemeId.FOREST,
    val warningProfile: WarningProfileId = WarningProfileId.DEFAULT,
) {
    val isValid: Boolean
        get() = seconds in TimerLimits.MIN_SECONDS..TimerLimits.MAX_SECONDS

    companion object {
        const val EXTRA_SECONDS = "vta_seconds"
        const val EXTRA_NAME = "vta_name"
        const val EXTRA_THEME = "vta_theme"
        const val EXTRA_PROFILE = "vta_profile"

        /** Accepts either the deep link query params or raw intent extras. */
        fun fromQueryParameters(
            seconds: Int?,
            name: String?,
            theme: String?,
            profile: String?,
        ): TimerRequest = TimerRequest(
            seconds = seconds ?: TimerLimits.MIN_SECONDS,
            presetName = name?.takeIf { it.isNotBlank() } ?: "Custom Timer",
            theme = ThemeId.fromName(theme),
            warningProfile = WarningProfileId.fromName(profile),
        )

        fun fromIntentExtras(
            seconds: Int?,
            name: String?,
            theme: String?,
            profile: String?,
        ): TimerRequest = fromQueryParameters(seconds, name, theme, profile)
    }
}
