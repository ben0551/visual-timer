package com.ben0551.visualtimeassistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.ben0551.visualtimeassistant.data.repo.PresetRepository
import com.ben0551.visualtimeassistant.domain.model.ClockTime
import com.ben0551.visualtimeassistant.domain.model.TimerRequest
import com.ben0551.visualtimeassistant.ui.navigation.AppNavHost
import com.ben0551.visualtimeassistant.ui.theme.VisualTimeAssistantTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var presetRepository: PresetRepository

    /** Latest automation request (deep link or intent extras), cleared once the UI shows it. */
    private var timerRequest by mutableStateOf<TimerRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            VisualTimeAssistantTheme {
                AppNavHost(
                    initialTimerRequest = timerRequest,
                    onTimerRequestConsumed = { timerRequest = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * Accepts either the deep link
     * `visualtime://timer?seconds=900&name=Custom&theme=FOREST&profile=DEFAULT`
     * (add `preset=Morning%20Routine` to launch a stored preset, or `until=8:45` for a
     * wall-clock target that becomes an "8:45 Timer" countdown)
     * or the raw extras `vta_seconds`, `vta_name`, `vta_theme`, `vta_profile`,
     * `vta_preset_id` / `vta_preset`, `vta_until`.
     */
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val data = intent.data
        val request = when {
            data != null && data.scheme == "visualtime" && data.host == "timer" ->
                TimerRequest.fromQueryParameters(
                    seconds = data.getQueryParameter("seconds")?.toIntOrNull(),
                    name = data.getQueryParameter("name"),
                    theme = data.getQueryParameter("theme"),
                    profile = data.getQueryParameter("profile"),
                    presetId = data.getQueryParameter("presetId")?.toLongOrNull(),
                    preset = data.getQueryParameter("preset"),
                    until = data.getQueryParameter("until"),
                )

            intent.hasExtra(TimerRequest.EXTRA_SECONDS) ||
                intent.hasExtra(TimerRequest.EXTRA_PRESET) ||
                intent.hasExtra(TimerRequest.EXTRA_PRESET_ID) ||
                intent.hasExtra(TimerRequest.EXTRA_UNTIL) -> TimerRequest.fromIntentExtras(
                seconds = intent.extras?.get(TimerRequest.EXTRA_SECONDS).toSecondsOrNull(),
                name = intent.extras?.get(TimerRequest.EXTRA_NAME)?.toString(),
                theme = intent.extras?.get(TimerRequest.EXTRA_THEME)?.toString(),
                profile = intent.extras?.get(TimerRequest.EXTRA_PROFILE)?.toString(),
                presetId = intent.extras?.get(TimerRequest.EXTRA_PRESET_ID).toPresetIdOrNull(),
                preset = intent.extras?.get(TimerRequest.EXTRA_PRESET)?.toString(),
                until = intent.extras?.get(TimerRequest.EXTRA_UNTIL)?.toString(),
            )

            else -> null
        }
        if (request == null || !request.isValid) return

        val selector = request.presetSelector
        if (selector != null) {
            // Resolve "Morning Routine" (or an id) against stored presets before launching.
            lifecycleScope.launchWhenStarted {
                val preset = presetRepository.find(selector)
                timerRequest = if (preset != null) {
                    TimerRequest(
                        seconds = preset.durationSec,
                        presetName = preset.name,
                        theme = preset.theme,
                        warningProfile = preset.warningProfile,
                        presetId = preset.id,
                    )
                } else {
                    null
                }
            }
        } else {
            timerRequest = request.resolveClockTime()
        }
    }

    /**
     * "Set a timer for 8:45": turn the wall-clock target into a countdown that starts now
     * and title it after the requested time. A malformed or impossible target is ignored,
     * which just opens the app normally.
     */
    private fun TimerRequest.resolveClockTime(): TimerRequest? {
        val clock = until ?: return this
        val seconds = ClockTime.secondsUntil(clock, System.currentTimeMillis()) ?: return null
        return copy(
            seconds = seconds,
            presetName = "$clock Timer",
            presetSelector = null,
            presetId = null,
            until = null,
        )
    }

    private fun Any?.toSecondsOrNull(): Int? = when (this) {
        is Int -> takeIf { it > 0 }
        is Long -> takeIf { it > 0 }?.toInt()
        is String -> toIntOrNull()?.takeIf { it > 0 }
        else -> null
    }

    private fun Any?.toPresetIdOrNull(): Long? = when (this) {
        is Int -> takeIf { it > 0 }?.toLong()
        is Long -> takeIf { it > 0 }
        is String -> toLongOrNull()?.takeIf { it > 0 }
        else -> null
    }
}
