package com.ben0551.visualtimeassistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ben0551.visualtimeassistant.domain.model.TimerRequest
import com.ben0551.visualtimeassistant.ui.navigation.AppNavHost
import com.ben0551.visualtimeassistant.ui.theme.VisualTimeAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
     * or the raw extras `vta_seconds`, `vta_name`, `vta_theme`, `vta_profile`.
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
                )

            intent.hasExtra(TimerRequest.EXTRA_SECONDS) -> TimerRequest.fromIntentExtras(
                seconds = intent.extras?.get(TimerRequest.EXTRA_SECONDS).toSecondsOrNull(),
                name = intent.extras?.get(TimerRequest.EXTRA_NAME)?.toString(),
                theme = intent.extras?.get(TimerRequest.EXTRA_THEME)?.toString(),
                profile = intent.extras?.get(TimerRequest.EXTRA_PROFILE)?.toString(),
            )

            else -> null
        }
        if (request != null && request.isValid) {
            timerRequest = request
        }
    }

    private fun Any?.toSecondsOrNull(): Int? = when (this) {
        is Int -> takeIf { it > 0 }
        is Long -> takeIf { it > 0 }?.toInt()
        is String -> toIntOrNull()?.takeIf { it > 0 }
        else -> null
    }
}
