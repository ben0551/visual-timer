package com.ben0551.visualtimeassistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ben0551.visualtimeassistant.data.repo.PresetRepository
import com.ben0551.visualtimeassistant.data.repo.SettingsRepository
import com.ben0551.visualtimeassistant.domain.model.AppSettings
import com.ben0551.visualtimeassistant.domain.model.Preset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    presetRepository: PresetRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val presets: StateFlow<List<Preset>> = presetRepository.presets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())
}
