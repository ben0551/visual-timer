package com.ben0551.visualtimeassistant.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ben0551.visualtimeassistant.data.repo.SettingsRepository
import com.ben0551.visualtimeassistant.domain.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun update(transform: (AppSettings) -> AppSettings) = viewModelScope.launch {
        settingsRepository.update(transform)
    }

    fun resetToDefaults() = viewModelScope.launch {
        settingsRepository.resetToDefaults()
    }
}
