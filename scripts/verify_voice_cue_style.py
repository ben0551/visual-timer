"""On-device check: the Voice cues toggle makes milestone speech say time left.

Usage: python3 scripts/verify_voice_cue_style.py <adb-serial>
Requires a debug build (VTAlert logging). Watches a 60 s STRICT timer; its 50%
milestone at 30 s is above the 15 s quiet window, so a VOICE alert is expected.
"""
import re
import subprocess
import sys
import time

serial = sys.argv[1]


def sh(*args):
    return subprocess.run(
        ["adb", "-s", serial, "shell", *args], capture_output=True, text=True
    ).stdout


def dump():
    sh("uiautomator", "dump", "/sdcard/vf.xml")
    return sh("cat", "/sdcard/vf.xml")


def find(xml, text):
    match = re.search(
        r'text="' + re.escape(text) + r'"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml
    )
    if not match:
        return None
    x1, y1, x2, y2 = map(int, match.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap(point):
    sh("input", "tap", str(point[0]), str(point[1]))


def alerts():
    out = subprocess.run(
        ["adb", "-s", serial, "logcat", "-d"],
        capture_output=True, text=True, errors="replace",
    ).stdout
    return [line for line in out.splitlines() if "VTAlert" in line and "VOICE" in line]


subprocess.run(["adb", "-s", serial, "logcat", "-c"])
sh("am", "force-stop", "com.ben0551.visualtimeassistant")
time.sleep(1)
sh("am", "start", "-n", "com.ben0551.visualtimeassistant/.MainActivity")
time.sleep(2)

settings = find(dump(), "Settings")
if not settings:
    sys.exit("INCONCLUSIVE: Settings nav item missing")
tap(settings)
time.sleep(1)

xml = dump()
chip = None
for _ in range(8):
    chip = find(xml, "Time left")
    if chip:
        break
    sh("input", "swipe", "960", "850", "960", "300")
    time.sleep(0.4)
    xml = dump()
if not chip:
    sys.exit("FAIL: 'Time left' voice-cue option is missing from Settings")
tap(chip)
time.sleep(1)
print("Selected 'Time left' in Settings")

sh("am", "force-stop", "com.ben0551.visualtimeassistant")
time.sleep(1)
sh(
    "am", "start", "-W", "-a", "android.intent.action.VIEW",
    "-d", "visualtime://timer?seconds=60&name=CUE_PROBE&theme=FOREST&profile=STRICT",
)

deadline = time.time() + 45
while time.time() < deadline:
    voice = alerts()
    if voice:
        break
    time.sleep(0.5)
else:
    sys.exit("INCONCLUSIVE: no voice milestone logged")

message = voice[-1].split("msg=", 1)[-1].strip()
print("Spoken:", message)
if not re.fullmatch(r"\d+ (?:minutes?|seconds?) remaining\.", message):
    sys.exit(f"FAIL: expected a time-remaining cue, got {message!r}")
print("PASS: milestone speech reports time remaining")
