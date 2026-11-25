"""End-to-end regression probe: no announcements may fire after DONE is pressed.

Usage: python3 repro_done_alerts.py <adb-serial>
Requires a debug build with VTAlert/VTSpeak logging (VTAlert is in HEAD, VTSpeak only
in the instrumented build). Scans logcat timestamps, so it works without tag filters.
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


def log_lines():
    out = subprocess.run(
        ["adb", "-s", serial, "logcat", "-d"], capture_output=True, text=True, errors="replace"
    ).stdout
    return [
        line
        for line in out.splitlines()
        if "VTAlert" in line or "VTSpeak" in line
    ]


def texts():
    sh("uiautomator", "dump", "/sdcard/vf.xml")
    xml = sh("cat", "/sdcard/vf.xml")
    return [t for t in re.findall(r'text="([^"]+)"', xml) if t.strip()]


subprocess.run(["adb", "-s", serial, "logcat", "-c"])
sh("am", "force-stop", "com.ben0551.visualtimeassistant")
time.sleep(1)
sh(
    "am", "start", "-W", "-a", "android.intent.action.VIEW",
    "-d", "visualtime://timer?seconds=60&name=DONE_PROBE&theme=FOREST&profile=STRICT",
)

# Wait until at least one spoken milestone has fired (STRICT: 75% at 15 s).
deadline = time.time() + 40
while time.time() < deadline:
    if any(
        "kind=VOICE" in line or "kind=DAD_MODE" in line or "kind=VOICE_HIGHLIGHT" in line
        for line in log_lines()
    ):
        break
    time.sleep(0.5)
else:
    sys.exit("INCONCLUSIVE: no milestone announcement fired")

# Tap DONE from the live UI hierarchy.
sh("uiautomator", "dump", "/sdcard/vf.xml")
xml = sh("cat", "/sdcard/vf.xml")
match = re.search(r'text="DONE"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml)
if not match:
    sys.exit("INCONCLUSIVE: DONE button not found")
x1, y1, x2, y2 = map(int, match.groups())
done_clock = time.strftime("%m-%d %H:%M:%S")
sh("input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
time.sleep(1)
home = any("Pick a timer and go." in t for t in texts())
if not home:
    sys.exit("INCONCLUSIVE: DONE tap did not leave the timer screen")

time.sleep(12)
late = []
for line in log_lines():
    stamp = line[:19]  # "11-25 20:04:51"
    if stamp > done_clock:
        late.append(line)

if late:
    print("FAIL: announcements after DONE:")
    print("\n".join("  " + line for line in late))
    sys.exit(1)
print("PASS: no announcements in the 12 s after DONE")
