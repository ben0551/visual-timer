package com.ben0551.visualtimeassistant.ui.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ben0551.visualtimeassistant.data.repo.SettingsRepository
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerLimits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomTimerUiState(
    val hours: Int = 0,
    val minutes: Int = 5,
    val seconds: Int = 0,
    val theme: ThemeId = ThemeId.FOREST,
) {
    val totalSeconds: Int get() = hours * 3600 + minutes * 60 + seconds

    val validationError: String? = when {
        totalSeconds < TimerLimits.MIN_SECONDS -> "Minimum is 10 seconds."
        totalSeconds > TimerLimits.MAX_SECONDS -> "Maximum is 24 hours."
        else -> null
    }

    val isValid: Boolean get() = validationError == null
}

@HiltViewModel
class CustomTimerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CustomTimerUiState())
    val state: StateFlow<CustomTimerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = runCatching { settingsRepository.current() }.getOrNull()
            if (settings != null) {
                _state.value = _state.value.copy(theme = settings.theme)
            }
        }
    }

    fun setHours(value: Int) = update { it.copy(hours = value.coerceIn(0, 23)) }

    fun setMinutes(value: Int) = update { it.copy(minutes = value.coerceIn(0, 59)) }

    fun setSeconds(value: Int) = update { it.copy(seconds = value.coerceIn(0, 59)) }

    fun stepHours(delta: Int) = setHours(_state.value.hours + delta)

    fun stepMinutes(delta: Int) = setMinutes(_state.value.minutes + delta)

    fun stepSeconds(delta: Int) = setSeconds(_state.value.seconds + delta)

    fun setTheme(theme: ThemeId) = update { it.copy(theme = theme) }

    private fun update(transform: (CustomTimerUiState) -> CustomTimerUiState) {
        _state.value = transform(_state.value)
    }
}
