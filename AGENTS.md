# Agent integration guide (AGENTS.md)

Visual Timer is built to be driven by an AI agent over **adb**, so any agent harness that can
run a shell command (Hermes, OpenClaw, Claude Code, Codex, OpenCode, a shell script, Tasker…)
can start timers with structured parameters - the same call pattern you would use for an MCP
tool, without needing an MCP server.

- App id: `com.ben0551.visualtimeassistant` (launcher label: **Visual Timer**)
- Helper scripts: [`scripts/tv_timer.sh`](scripts/tv_timer.sh), [`scripts/tv_preset.sh`](scripts/tv_preset.sh)
- Deep-link contract lives in `MainActivity.handleIntent` and `domain/model/TimerRequest.kt`

## Start a timer

```bash
# Duration-based
./scripts/tv_timer.sh <minutes> [theme] [name]
./scripts/tv_timer.sh 15 POTATO "Morning Routine"

# Stored preset by name or id (resolved inside the app)
./scripts/tv_preset.sh "Morning Routine"
```

Equivalent raw intents (what the scripts run):

```bash
adb shell "am start -W -a android.intent.action.VIEW \
  -d 'visualtime://timer?seconds=900&name=Morning%20Routine&theme=POTATO&profile=DEFAULT'"

adb shell am start -n com.ben0551.visualtimeassistant/.MainActivity \
  --ei vta_seconds 900 --es vta_name "Morning Routine" --es vta_theme POTATO
```

## Parameters

| Parameter | Where | Values / notes |
| --- | --- | --- |
| `seconds` | query or `--ei vta_seconds` | 10 … 86400. Out-of-range requests are ignored (app opens normally). |
| `name` | query or `--es vta_name` | Label shown on the running screen; defaults to "Custom Timer". |
| `theme` | query or `--es vta_theme` | `FOREST` `ROCKET` `AQUARIUM` `SPACE` `POTATO` `RANDOM`; unknown = `FOREST`. |
| `profile` | query or `--es vta_profile` | `DEFAULT` `GENTLE` `STRICT` warning profiles. |
| `preset` | query or `--es vta_preset` | Preset **name** (case-insensitive) or numeric id. The app resolves duration/theme/profile itself. Unknown names start nothing. |
| `presetId` | query or `--ei vta_preset_id` | Direct preset id. |

## Behaviour worth knowing

- A request while another timer runs **replaces** it (only one timer at a time).
- `Settings → Count overtime` controls whether zero keeps counting as "LATE BY"; default on.
- `Settings → Mute mode` silences chimes and voice; `Voice prompts` silences speech only.
- TTS uses Android's engine, so voice depends on the device having a TTS language installed.
- Everything works offline. No accounts, no permissions beyond none at all.

## Verify a launch

```bash
adb shell uiautomator dump /sdcard/v.xml && adb pull /sdcard/v.xml - | grep -o 'text="[^"]*"'
```

You should see the preset name uppercased, the countdown near the requested duration, and
`DONE / PAUSE / STOP / MUTE`.

## Build & test

```bash
./gradlew assembleDebug testDebugUnitTest    # APK: app/build/outputs/apk/debug/
```
