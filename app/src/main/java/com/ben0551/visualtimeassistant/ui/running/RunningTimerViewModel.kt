package com.ben0551.visualtimeassistant.ui.running

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ben0551.visualtimeassistant.data.repo.TimerRepository
import com.ben0551.visualtimeassistant.domain.model.AppSettings
import com.ben0551.visualtimeassistant.domain.model.DisplayMode
import com.ben0551.visualtimeassistant.domain.model.StatusText
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.TimerLimits
import com.ben0551.visualtimeassistant.domain.model.TimerSessionRecord
import com.ben0551.visualtimeassistant.domain.model.VoiceMessages
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import com.ben0551.visualtimeassistant.service.ChimePlayer
import com.ben0551.visualtimeassistant.service.SpeakerAnnouncer
import com.ben0551.visualtimeassistant.timer.AlertKind
import com.ben0551.visualtimeassistant.timer.MilestoneEvaluator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Everything the running-timer screen renders, in one immutable snapshot. */
data class TimerUiState(
    val totalSec: Int = 0,
    val elapsedSec: Int = 0,
    val remainingSec: Int = 0,
    val phase: Phase = Phase.RUNNING,
    val presetName: String = "Custom Timer",
    val displayTheme: ThemeId = ThemeId.FOREST,
    val displayMode: DisplayMode = DisplayMode.BAR,
    val dadModeActive: Boolean = false,
    val dadModeThresholdPercent: Int = TimerLimits.DAD_MODE_DEFAULT_THRESHOLD_PERCENT,
    val muted: Boolean = false,
    val statusText: String = StatusText.GETTING_STARTED,
    val showCountdown: Boolean = true,
    val showPercentage: Boolean = true,
    val showProgressBar: Boolean = true,
    val largeText: Boolean = false,
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
    val colourBlindPalette: Boolean = false,
) {
    enum class Phase { RUNNING, PAUSED, EXPIRED }

    val elapsedFraction: Float
        get() = if (totalSec <= 0) 0f else (elapsedSec.toFloat() / totalSec).coerceIn(0f, 1f)

    val isOvertime: Boolean get() = remainingSec < 0
    val overtimeSec: Int get() = (-remainingSec).coerceAtLeast(0)
}

@HiltViewModel
class RunningTimerViewModel @Inject constructor(
    private val timerRepository: TimerRepository,
    private val settingsRepository: com.ben0551.visualtimeassistant.data.repo.SettingsRepository,
    private val speakerAnnouncer: SpeakerAnnouncer,
    private val chimePlayer: ChimePlayer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private val _finished = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    /** Emits true when the run was recorded (DONE pressed, or expired then closed). */
    val finished: SharedFlow<Boolean> = _finished.asSharedFlow()

    private var evaluator: MilestoneEvaluator? = null
    private var settings: AppSettings = AppSettings()
    private var presetId: Long? = null
    private var warningProfile: WarningProfileId = WarningProfileId.DEFAULT
    private var startWallClock: Long = 0L

    private var tickJob: Job? = null
    private var anchorElapsedMs: Long = 0L
    private var accumulatedMs: Long = 0L
    private var lastRemainingSec: Int = 0
    private var recorded = false
    private var started = false
    private var mutedNow = false
    private var exitRequested = false

    /** Called once per composition with the nav arguments; restarts if they change. */
    fun start(
        totalSec: Int,
        presetId: Long?,
        presetName: String,
        theme: ThemeId,
        warningProfile: WarningProfileId,
    ) {
        val resolvedTheme = theme.resolve()
        val displayName = presetName.ifBlank { "Custom Timer" }
        // Navigation composes the destination once with default arguments before the real
        // ones land; ignore no-op repeats but re-configure when the request differs.
        if (started && !exitRequested &&
            _uiState.value.totalSec == totalSec &&
            _uiState.value.presetName == displayName &&
            _uiState.value.displayTheme == resolvedTheme &&
            this.warningProfile == warningProfile
        ) {
            return
        }
        started = true
        exitRequested = false
        this.presetId = presetId
        this.warningProfile = warningProfile
        startWallClock = System.currentTimeMillis()

        viewModelScope.launch {
            settings = runCatching { settingsRepository.current() }.getOrDefault(AppSettings())
            mutedNow = settings.muteMode
            evaluator = MilestoneEvaluator(
                totalSeconds = totalSec,
                profile = warningProfile,
                dadModeEnabled = settings.dadModeEnabled,
                dadThresholdPercent = settings.dadModeThresholdPercent,
                customMilestones = settings.customMilestones,
            )
            anchorElapsedMs = SystemClock.elapsedRealtime()
            accumulatedMs = 0L
            lastRemainingSec = totalSec
            _uiState.value = TimerUiState(
                totalSec = totalSec,
                remainingSec = totalSec,
                phase = TimerUiState.Phase.RUNNING,
                presetName = displayName,
                displayTheme = theme,
                dadModeThresholdPercent = settings.dadModeThresholdPercent,
                muted = settings.muteMode,
                displayMode = settings.displayMode,
                showCountdown = settings.showCountdown,
                showPercentage = settings.showPercentage,
                showProgressBar = settings.showProgressBar,
                largeText = settings.largeText,
                highContrast = settings.highContrast,
                reducedMotion = settings.reducedMotion,
                colourBlindPalette = settings.colourBlindPalette,
            )
            startTicking()
        }
    }

    fun pauseOrResume() {
        val state = _uiState.value
        when (state.phase) {
            TimerUiState.Phase.PAUSED -> {
                anchorElapsedMs = SystemClock.elapsedRealtime()
                updatePhase(TimerUiState.Phase.RUNNING)
                startTicking()
            }
            else -> {
                accumulatedMs += SystemClock.elapsedRealtime() - anchorElapsedMs
                tickJob?.cancel()
                updatePhase(TimerUiState.Phase.PAUSED)
            }
        }
    }

    /** Live mute from the timer screen; persists to settings so it survives restarts. */
    fun toggleMute() {
        val next = !mutedNow
        mutedNow = next
        _uiState.value = _uiState.value.copy(muted = next)
        viewModelScope.launch {
            runCatching { settingsRepository.update { it.copy(muteMode = next) } }
        }
    }

    /** Parent confirms the task is done: records the session and leaves the screen. */
    fun done() {
        stopRun()
        record()
        emitFinished()
    }

    /** Back press: keeps overtime data (that is the useful part), drops unfinished runs. */
    fun abandon() {
        stopRun()
        if (_uiState.value.isOvertime) record() else recorded = true
        emitFinished()
    }

    /**
     * Leaving the running screen must silence the timer immediately. Otherwise the ticking
     * coroutine can keep crossing milestones and the singleton TTS keeps talking about time
     * remaining after DONE/STOP has been pressed.
     */
    private fun stopRun() {
        exitRequested = true
        tickJob?.cancel()
        speakerAnnouncer.stop()
    }

    private fun emitFinished() {
        viewModelScope.launch { _finished.emit(recorded) }
    }

    private fun record() {
        if (recorded) return
        recorded = true
        val state = _uiState.value
        val actualSec = state.elapsedSec.coerceAtLeast(0)
        val session = TimerSessionRecord(
            presetId = presetId,
            presetName = state.presetName,
            startTime = startWallClock,
            endTime = System.currentTimeMillis(),
            plannedDurationSec = state.totalSec,
            actualDurationSec = actualSec,
            overtimeSec = actualSec - state.totalSec,
            completedOnTime = actualSec <= state.totalSec,
            themeUsed = state.displayTheme,
            warningProfileId = warningProfile,
            displayMode = settings.displayMode,
        )
        timerRepository.record(session)
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                val elapsedMs = accumulatedMs + (SystemClock.elapsedRealtime() - anchorElapsedMs)
                val elapsedSec = (elapsedMs / 1000L).toInt().coerceAtLeast(0)
                val remainingSec = _uiState.value.totalSec - elapsedSec
                publish(elapsedSec, remainingSec)
                if (exitRequested) break
                evaluator?.onTick(lastRemainingSec, remainingSec)?.forEach { alert ->
                    if (exitRequested) return@forEach
                    handleAlert(alert)
                }
                lastRemainingSec = remainingSec
                delay(TICK_MS)
            }
        }
    }

    private fun publish(elapsedSec: Int, remainingSec: Int) {
        val state = _uiState.value
        val expired = remainingSec <= 0
        // Overtime kill-switch: stop exactly at zero instead of counting late time.
        val shownRemaining = if (expired && !settings.overtimeEnabled) 0 else remainingSec
        val dadThresholdSec = state.totalSec * settings.dadModeThresholdPercent / 100.0
        val dadModeActive = settings.dadModeEnabled && when {
            remainingSec <= 0 -> settings.overtimeEnabled
            else -> remainingSec <= dadThresholdSec
        }
        _uiState.value = state.copy(
            elapsedSec = elapsedSec,
            remainingSec = shownRemaining,
            phase = if (expired) TimerUiState.Phase.EXPIRED else state.phase,
            dadModeActive = dadModeActive,
            statusText = if (expired) StatusText.TIME_IS_UP
            else StatusText.of(state.elapsedFraction, remainingSec, state.overtimeSec),
        )
        if (expired && !settings.overtimeEnabled) {
            tickJob?.cancel()
        }
    }

    private fun updatePhase(phase: TimerUiState.Phase) {
        _uiState.value = _uiState.value.copy(phase = phase)
    }

    private fun handleAlert(alert: com.ben0551.visualtimeassistant.timer.TimerAlert) {
        if (exitRequested) return
        android.util.Log.i("VTAlert", "kind=${alert.kind} percent=${alert.percent} msg=${alert.message}")
        val muted = mutedNow
        val voiceOn = settings.voiceEnabled && !muted
        when (alert.kind) {
            AlertKind.CHIME -> if (!muted) chimePlayer.chime()
            AlertKind.VOICE -> if (voiceOn) speakerAnnouncer.speak(alert.message)
            AlertKind.VOICE_HIGHLIGHT -> {
                if (!muted) chimePlayer.chime()
                if (voiceOn) speakerAnnouncer.speak(alert.message)
            }
            AlertKind.DAD_MODE -> {
                if (!muted) chimePlayer.completion()
                if (voiceOn) speakerAnnouncer.speak(alert.message)
            }
            AlertKind.EXPIRED -> {
                if (!muted) chimePlayer.completion()
                if (voiceOn) speakerAnnouncer.speak(VoiceMessages.TIME_IS_UP)
            }
        }
    }

    override fun onCleared() {
        exitRequested = true
        tickJob?.cancel()
        speakerAnnouncer.stop()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 200L
    }
}
