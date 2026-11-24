# Visual Time Assistant

An Android app that makes the passage of time **visual** for children aged 6–12, built for
**Android TV** first, with tablet and phone support from the same codebase.

Instead of an abstract countdown, remaining time is shown as a growing bar, filling pie,
block countdown and an animated environment (a plant growing, a rocket climbing, fish
appearing in an aquarium...). When the timer expires, overtime keeps counting so parents get
real data about how long tasks actually take.

## Features (v1.0)

| Area | What's implemented |
| --- | --- |
| Home | Large quick-start cards (5/10/15/30 min, custom) plus the family's presets |
| Custom timer | Hours/minutes/seconds steppers, 10 s – 24 h validation |
| Running timer | Countdown sized for 5–10 m legibility, elapsed-growth progress, pause/resume |
| Progress modes | Bar, circular, blocks, combined (bar + percentage) |
| Themes | Forest Growth, Rocket Launch, Aquarium, Space Journey, Potato Empire™, Random |
| Warning system | 50 % chime → 25 % voice → 10 % voice + highlight → 5 % Dad Mode → expiry |
| Dad Mode | Adjustable 1–50 % threshold: dim, pulsing bar, flashing amber frame, spoken prompt |
| Overtime | Timer keeps running past zero ("TIME IS UP · LATE BY 00:34") until DONE |
| Analytics | Today's runs/on-time/late, 7 & 30 day average overtime, most difficult / easiest tasks |
| Presets | Parent-editable name, duration, theme, warning profile, favourite |
| Settings | Display toggles, progress style, theme, voice prompts, custom milestones, accessibility |
| Accessibility | High contrast, large text, reduced motion, colour-blind palette, mute mode |
| Platforms | D-Pad/OK/Back only operation on TV; touch-friendly layouts on tablet/phone |
| Offline | 100 % offline, no accounts, no ads, no telemetry |

## Tech stack

Kotlin · Jetpack Compose (Material 3) · Compose Navigation · Room · Hilt · MVVM · Coroutines/Flow.

```
ui (Composables) → ViewModel (StateFlow) → Repository → Room DAO → SQLite
```

One Compose UI adapts by window width: bottom navigation bar below 600 dp, navigation rail above.
The running timer is full-screen with no chrome.

## Build

Requires JDK 17 and the Android SDK (compileSdk 35).

```bash
echo "sdk.dir=/path/to/Android/Sdk" > local.properties   # or set $ANDROID_HOME
./gradlew assembleDebug            # APK at app/build/outputs/apk/debug/
./gradlew testDebugUnitTest        # JVM unit tests
```

Release builds are signed with the debug key so they can be sideloaded onto a TV as-is.

## Project layout

```
app/src/main/java/com/ben0551/visualtimeassistant/
├── VisualTimeApp.kt            # @HiltAndroidApp, seeds presets/settings on first launch
├── MainActivity.kt             # single activity, edge-to-edge Compose host
├── data/                       # Room entities, DAOs, DB, repositories, mappers
├── domain/model/               # ThemeId, DisplayMode, WarningProfileId, settings, records
├── domain/stats/               # StatsCalculator (pure, unit tested)
├── timer/MilestoneEvaluator.kt # warning engine (pure, unit tested)
├── service/                    # SpeakerAnnouncer (TTS), ChimePlayer (ToneGenerator)
└── ui/
    ├── navigation/             # routes + adaptive shell (bottom bar / rail)
    ├── theme/                  # dark palette, high-contrast palette, progress ramp
    ├── components/             # progress visuals, animated themes, D-pad focus helpers
    └── home|custom|running|presets|stats|settings/
```

### Timing accuracy

The timer anchors to `SystemClock.elapsedRealtime()` (monotonic clock) and recomputes elapsed
time every 200 ms tick, so it does not drift even if the process is throttled. Pausing banks
the accumulated time before stopping the ticker.

### Data recorded per session

`TimerSession` stores start/end timestamps, planned vs actual duration, overtime (signed),
completed-on-time flag, theme, warning profile and display mode. Overtime is negative when a
task finished early, which powers the "easiest tasks" list.

## Roadmap (post-1.0)

Companion parent remote · child profiles · custom recorded parent voice · routine chains ·
achievements · household sync · Wear OS notifications · AI duration recommendations.
