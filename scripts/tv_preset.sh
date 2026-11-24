#!/usr/bin/env bash
# Launch a stored preset ("Morning Routine", "Bed Time", ...) on the TV/tablet over adb.
#
# Usage:  tv_preset.sh "<preset name or id>"
# Example: tv_preset.sh "Morning Routine"
#
# The app resolves the selector against its own preset library (case-insensitive name or
# numeric id) and starts that exact duration, theme and warning profile. Unknown selectors
# open the app normally without starting anything.
set -euo pipefail

SELECTOR="${1:?usage: tv_preset.sh \"<preset name or id>\"}"

ADB="${GOOGLE_TV_ADB:-/opt/data/home/tools/platform-tools/adb}"
if [ ! -x "$ADB" ]; then
  echo "adb not found at $ADB (set GOOGLE_TV_ADB)" >&2
  exit 1
fi

SERIAL="${ANDROID_SERIAL:-$("$ADB" devices | awk '$2 == "device" {print $1; exit}')}"
if [ -z "$SERIAL" ]; then
  echo "No adb device online. Pair/connect first:" >&2
  echo "  python3 /opt/data/skills/nas-service-exposure/google-tv-adb/scripts/gtv.py connect" >&2
  exit 1
fi

ENCODED="$(python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1]))' "$SELECTOR")"
URL="visualtime://timer?preset=${ENCODED}"

"$ADB" -s "$SERIAL" shell "am start -W -a android.intent.action.VIEW -d '$URL'" >/dev/null

echo "Requested preset '${SELECTOR}' on ${SERIAL} (resolved inside the app)"
