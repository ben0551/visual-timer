package com.ben0551.visualtimeassistant.ui.running

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ben0551.visualtimeassistant.domain.model.DisplayMode
import com.ben0551.visualtimeassistant.domain.model.ThemeId
import com.ben0551.visualtimeassistant.domain.model.VoiceMessages
import com.ben0551.visualtimeassistant.domain.model.WarningProfileId
import com.ben0551.visualtimeassistant.ui.components.AnimatedThemeBackground
import com.ben0551.visualtimeassistant.ui.components.BlockCountdown
import com.ben0551.visualtimeassistant.ui.components.CircularCountdown
import com.ben0551.visualtimeassistant.ui.components.CombinedProgress
import com.ben0551.visualtimeassistant.ui.components.ElapsedProgressBar
import com.ben0551.visualtimeassistant.ui.components.tvFocusScale
import com.ben0551.visualtimeassistant.ui.components.TvSecondaryButton
import com.ben0551.visualtimeassistant.ui.theme.ProgressPalette
import com.ben0551.visualtimeassistant.util.DurationFormatter

/**
 * Screen 3 - the running timer.
 * Priority order: countdown (~40% height), visual progress, animated background, status text.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RunningTimerScreen(
    totalSec: Int,
    presetId: Long?,
    presetName: String,
    theme: ThemeId,
    warningProfile: WarningProfileId,
    onExit: () -> Unit,
) {
    val viewModel: RunningTimerViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(totalSec, presetId, presetName, theme) {
        if (totalSec > 0) {
            viewModel.start(totalSec, presetId, presetName, theme.resolve(), warningProfile)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.finished.collect { onExit() }
    }
    BackHandler { viewModel.abandon() }

    // A visible timer must never let the screen sleep.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val progressColor = ProgressPalette.color(state.elapsedFraction, state.colourBlindPalette)

    val transition = rememberInfiniteTransition(label = "urgency")
    val pulseRaw by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val dadPulse = if (state.dadModeActive && !state.reducedMotion) pulseRaw else 0f

    val dimAlpha by animateFloatAsState(
        targetValue = if (state.dadModeActive) 0.45f else 0f,
        animationSpec = tween(400),
        label = "dadDim",
    )

    Box(Modifier.fillMaxSize()) {
        AnimatedThemeBackground(
            theme = state.displayTheme,
            progress = state.elapsedFraction,
            reducedMotion = state.reducedMotion,
            modifier = Modifier.fillMaxSize(),
        )

        // Dad Mode: dim everything, flash an amber frame.
        if (dimAlpha > 0.01f) {
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = dimAlpha)))
        }
        if (dadPulse > 0f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = 14.dp,
                        color = Color(0xFFFFC107).copy(alpha = dadPulse),
                    ),
            )
        }
        // Timer finished: red tint over the whole screen.
        if (state.phase == TimerUiState.Phase.EXPIRED) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0xFFD32F2F).copy(alpha = if (state.reducedMotion) 0.18f else 0.10f + 0.10f * dadPulse)),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = state.presetName.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))

            if (state.isOvertime) {
                Text(
                    text = VoiceMessages.TIME_IS_UP,
                    fontSize = if (state.largeText) 40.sp else 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "LATE BY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CountdownText(
                    seconds = state.overtimeSec,
                    color = Color(0xFFFF5252),
                    largeText = state.largeText,
                )
            } else if (state.phase == TimerUiState.Phase.EXPIRED) {
                // Overtime disabled: the timer simply ends at zero.
                Text(
                    text = VoiceMessages.TIME_IS_UP,
                    fontSize = if (state.largeText) 96.sp else 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                )
            } else if (state.showCountdown) {
                CountdownText(
                    seconds = state.remainingSec,
                    color = if (state.dadModeActive) Color(0xFFFFC107) else MaterialTheme.colorScheme.onBackground,
                    largeText = state.largeText,
                )
            }

            Spacer(Modifier.height(12.dp))
            if (state.dadModeActive && !state.isOvertime) {
                Text(
                    text = VoiceMessages.dadModeBanner(
                        thresholdPercent = state.dadModeThresholdPercent,
                        totalSeconds = state.totalSec,
                    ),
                    fontSize = if (state.largeText) 32.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFC107),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = state.statusText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            val fraction = state.elapsedFraction
                val barAlpha = if (state.dadModeActive && !state.reducedMotion) 0.5f + 0.5f * dadPulse else 1f
                val percentText = "${DurationFormatter.remainingPercent(state.totalSec, state.remainingSec)}%"
                Box(Modifier.width(640.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (state.displayMode) {
                        DisplayMode.BAR -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (state.showPercentage) {
                                Text(
                                    percentText,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor.copy(alpha = barAlpha),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            ElapsedProgressBar(
                                fraction = fraction,
                                color = progressColor.copy(alpha = barAlpha),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        DisplayMode.CIRCULAR -> CircularCountdown(
                            fraction = fraction,
                            color = progressColor.copy(alpha = barAlpha),
                            strokeWidth = 26.dp,
                            modifier = Modifier.size(240.dp),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (state.showCountdown) DurationFormatter.clock(state.remainingSec) else percentText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (state.largeText) 40.sp else 30.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                if (state.showPercentage && state.showCountdown) {
                                    Text(
                                        percentText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        DisplayMode.BLOCK -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (state.showPercentage) {
                                Text(
                                    percentText,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor.copy(alpha = barAlpha),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            BlockCountdown(fraction = fraction, color = progressColor.copy(alpha = barAlpha))
                        }
                        DisplayMode.COMBINED -> CombinedProgress(
                            fraction = fraction,
                            percentText = percentText,
                            color = progressColor.copy(alpha = barAlpha),
                            largeText = state.largeText,
                        )
                    }
                }

            Spacer(Modifier.height(24.dp))

            // Wraps onto a second line on narrow phones so every control stays reachable.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { viewModel.done() },
                    modifier = Modifier.height(72.dp).width(180.dp).tvFocusScale(cornerRadius = 20.dp),
                ) {
                    Text(
                        "DONE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1200),
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.pauseOrResume() },
                    modifier = Modifier.height(72.dp).width(140.dp).tvFocusScale(cornerRadius = 20.dp),
                ) {
                    Text(
                        if (state.phase == TimerUiState.Phase.PAUSED) "RESUME" else "PAUSE",
                        fontSize = 18.sp,
                    )
                }
                TvSecondaryButton(
                    onClick = { viewModel.abandon() },
                    modifier = Modifier.height(72.dp).width(120.dp).tvFocusScale(cornerRadius = 20.dp),
                ) {
                    Text("STOP", fontSize = 18.sp)
                }
                TvSecondaryButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier.height(72.dp).width(160.dp).tvFocusScale(cornerRadius = 20.dp),
                ) {
                    Icon(
                        imageVector = if (state.muted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(if (state.muted) "UNMUTE" else "MUTE", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = "DONE = finished, time saved   ·   STOP = cancel the timer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (state.phase == TimerUiState.Phase.PAUSED) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Paused",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

/** Countdown sized relative to available width so it stays readable from 5-10 metres. */
@Composable
private fun CountdownText(seconds: Int, color: Color, largeText: Boolean) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val fontSize = (maxWidth.value * (if (largeText) 0.24f else 0.19f)).coerceIn(48f, 160f)
        Text(
            text = DurationFormatter.clock(seconds),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}
