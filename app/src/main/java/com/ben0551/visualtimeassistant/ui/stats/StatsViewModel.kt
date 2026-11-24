package com.ben0551.visualtimeassistant.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ben0551.visualtimeassistant.data.repo.TimerRepository
import com.ben0551.visualtimeassistant.domain.model.StatsSummary
import com.ben0551.visualtimeassistant.domain.stats.StatsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    timerRepository: TimerRepository,
) : ViewModel() {

    /** Recomputed whenever a session is recorded. */
    val summary: StateFlow<StatsSummary> = timerRepository.sessions
        .map { sessions -> StatsCalculator.compute(sessions, System.currentTimeMillis()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsSummary())
}
