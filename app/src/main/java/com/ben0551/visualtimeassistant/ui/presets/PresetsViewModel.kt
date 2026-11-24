package com.ben0551.visualtimeassistant.ui.presets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ben0551.visualtimeassistant.data.repo.PresetRepository
import com.ben0551.visualtimeassistant.domain.model.Preset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresetsViewModel @Inject constructor(
    private val presetRepository: PresetRepository,
) : ViewModel() {

    val presets: StateFlow<List<Preset>> = presetRepository.presets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleFavourite(preset: Preset) = viewModelScope.launch {
        presetRepository.toggleFavourite(preset)
    }

    fun delete(preset: Preset) = viewModelScope.launch {
        presetRepository.delete(preset)
    }

    fun save(preset: Preset) = viewModelScope.launch {
        presetRepository.save(preset)
    }
}
