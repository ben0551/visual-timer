package com.ben0551.visualtimeassistant.domain.model

/**
 * External "start this timer now" request, delivered by an AI agent (or any automation)
 * through the visualtime:// deep link or intent extras.
 *
 * [presetSelector] lets automation ask for a stored preset by name ("Morning Routine") or
 * numeric id; the app resolves it to a concrete duration/theme/profile before navigating.
 */
data class TimerRequest(
    val seconds: Int,
    val presetName: String = "Custom Timer",
    val theme: ThemeId = ThemeId.FOREST,
    val warningProfile: WarningProfileId = WarningProfileId.DEFAULT,
    val presetId: Long? = null,
    val presetSelector: String? = null,
    /** Wall-clock target ("8:45") - resolved to [seconds] by the app using the device clock. */
    val until: String? = null,
) {
    val isValid: Boolean
        get() = seconds in TimerLimits.MIN_SECONDS..TimerLimits.MAX_SECONDS ||
            presetSelector != null ||
            !until.isNullOrBlank()

    companion object {
        const val EXTRA_SECONDS = "vta_seconds"
        const val EXTRA_NAME = "vta_name"
        const val EXTRA_THEME = "vta_theme"
        const val EXTRA_PROFILE = "vta_profile"
        const val EXTRA_PRESET_ID = "vta_preset_id"
        const val EXTRA_PRESET = "vta_preset"
        const val EXTRA_UNTIL = "vta_until"

        /** Accepts either the deep link query params or raw intent extras. */
        fun fromQueryParameters(
            seconds: Int?,
            name: String?,
            theme: String?,
            profile: String?,
            presetId: Long? = null,
            preset: String? = null,
            until: String? = null,
        ): TimerRequest = TimerRequest(
            seconds = seconds ?: TimerLimits.MIN_SECONDS,
            presetName = name?.takeIf { it.isNotBlank() } ?: "Custom Timer",
            theme = ThemeId.fromName(theme),
            warningProfile = WarningProfileId.fromName(profile),
            presetId = presetId?.takeIf { it > 0 },
            presetSelector = preset?.takeIf { it.isNotBlank() },
            until = until?.trim()?.takeIf { it.isNotBlank() },
        )

        fun fromIntentExtras(
            seconds: Int?,
            name: String?,
            theme: String?,
            profile: String?,
            presetId: Long? = null,
            preset: String? = null,
            until: String? = null,
        ): TimerRequest = fromQueryParameters(seconds, name, theme, profile, presetId, preset, until)
    }
}
