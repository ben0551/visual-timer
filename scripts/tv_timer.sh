#!/usr/bin/env bash
# Start a Visual Time Assistant timer on Ben's Google TV over adb.
#
# Usage:  tv_timer.sh <minutes|HH:MM> [theme] [name]
# Example: tv_timer.sh 15 FOREST "Morning Routine"
#          tv_timer.sh 8:45                      # countdown until 8:45, titled "8:45 Timer"
#
# A value containing ":" or "." is treated as a wall-clock time: the app works out how long
# it is from now until that time (today, or tomorrow once it has passed) and starts the
# countdown with the title "<time> Timer".
#
# The app is launched through its deep link, which drops the family straight into the
# running timer (voice prompts, warnings and overtime all active).
set -euo pipefail

TARGET="${1:?usage: tv_timer.sh <minutes|HH:MM> [theme] [name]}"
THEME="${2:-FOREST}"
NAME="${3:-Custom Timer}"

ADB="${GOOGLE_TV_ADB:-/opt/data/home/tools/platform-tools/adb}"

if [ ! -x "$ADB" ]; then
  echo "adb not found at $ADB (set GOOGLE_TV_ADB)" >&2
  exit 1
fi

SERIAL="$("$ADB" devices | awk '$2 == "device" {print $1; exit}')"
if [ -z "$SERIAL" ]; then
  echo "No adb device online. Pair/connect first:" >&2
  echo "  python3 /opt/data/skills/nas-service-exposure/google-tv-adb/scripts/gtv.py connect" >&2
  exit 1
fi

# The TV sleeps; a deep link aimed at a dark screen is silently ignored.
"$ADB" -s "$SERIAL" shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1 || true

ENCODED_NAME="$(python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1]))' "$NAME")"

case "$TARGET" in
  *[:.]*)
    URL="visualtime://timer?until=${TARGET}&theme=${THEME}&profile=DEFAULT"
    ;;
  *)
    URL="visualtime://timer?seconds=$((TARGET * 60))&name=${ENCODED_NAME}&theme=${THEME}&profile=DEFAULT"
    ;;
esac

"$ADB" -s "$SERIAL" shell "am start -W -a android.intent.action.VIEW -d '$URL'" >/dev/null

case "$TARGET" in
  *[:.]*) echo "Started ${TARGET} timer on ${SERIAL} (countdown until ${TARGET}, theme: ${THEME})" ;;
  *)      echo "Started ${TARGET}-minute timer on ${SERIAL} (name: ${NAME}, theme: ${THEME})" ;;
esac
