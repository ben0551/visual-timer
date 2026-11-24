#!/usr/bin/env bash
# Start a Visual Time Assistant timer on Ben's Google TV over adb.
#
# Usage:  tv_timer.sh <minutes> [theme] [name]
# Example: tv_timer.sh 15 FOREST "Morning Routine"
#
# The app is launched through its deep link, which drops the family straight into the
# running timer (voice prompts, warnings and overtime all active).
set -euo pipefail

MINUTES="${1:?usage: tv_timer.sh <minutes> [theme] [name]}"
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

ENCODED_NAME="$(python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1]))' "$NAME")"
URL="visualtime://timer?seconds=$((MINUTES * 60))&name=${ENCODED_NAME}&theme=${THEME}&profile=DEFAULT"

"$ADB" -s "$SERIAL" shell "am start -W -a android.intent.action.VIEW -d '$URL'" >/dev/null

echo "Started ${MINUTES}-minute timer on ${SERIAL} (name: ${NAME}, theme: ${THEME})"
