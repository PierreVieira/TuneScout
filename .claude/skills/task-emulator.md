# Task emulator

Shared by `start-task` and `finish-task`: each task gets an emulator of its own, so its device work
never lands on an emulator someone else is using.

Several sessions run at once, and every emulator on the machine is visible to all of them. A
`connected*AndroidTest` installs on every device it sees. A benchmark or a flow test then runs
against an app that another session just replaced, and on a screen whose rotation someone else
changed. The user's own emulators (`Medium_Phone`, `Pixel_Tablet`, …) are theirs to look at, so
tests never run on them.

## Name

`TuneScout_<short_description>`: the task's short description from `start-task`, with `-` turned
into `_`. For `feature/player-beside-tabs`, that is `TuneScout_player_beside_tabs`. The name follows
from the branch, so `finish-task` finds the same emulator without anything written down.

## Create

`start-task` does this right after it creates the branch. It only writes two small config files. No
disk image is copied, and the emulator only takes space once it boots for the first time.

The copy is taken from `Pixel_9`: a phone whose landscape is wide enough for the two-pane layouts.
If `emulator -list-avds` doesn't list `Pixel_9`, ask the user which AVD to copy.

```bash
AVD=TuneScout_<short_description>
SOURCE=Pixel_9
AVD_HOME=~/.android/avd
ls -d "$AVD_HOME/$AVD.avd" "$AVD_HOME/$AVD.ini" 2>/dev/null
```

If either path already exists, stop and tell the user, the way `start-task` does for a branch that
already exists. Otherwise:

```bash
mkdir "$AVD_HOME/$AVD.avd"
cp "$AVD_HOME/$SOURCE.avd/config.ini" "$AVD_HOME/$AVD.avd/config.ini"
sed -i '' "s/^AvdId=.*/AvdId=$AVD/; s/^avd.ini.displayname=.*/avd.ini.displayname=$AVD/" \
  "$AVD_HOME/$AVD.avd/config.ini"
printf 'avd.ini.encoding=UTF-8\npath=%s\npath.rel=avd/%s.avd\ntarget=%s\n' \
  "$AVD_HOME/$AVD.avd" "$AVD" "$(sed -n 's/^target=//p' "$AVD_HOME/$SOURCE.ini")" \
  > "$AVD_HOME/$AVD.ini"
```

Only `config.ini` is copied. Copying the source's `userdata` or `snapshots` would carry over its
apps, its data and its rotation.

## Boot

Boot the emulator only when the task needs a device. Give it a console port outside the range adb
scans on its own (5554–5585), and an adb server of its own:

```bash
CONSOLE_PORT=<an even port from 5700 to 5798 that `lsof -iTCP:<port>` shows free>
export ANDROID_ADB_SERVER_PORT=$((CONSOLE_PORT + 1000))
nohup ~/Library/Android/sdk/emulator/emulator -avd "$AVD" -port "$CONSOLE_PORT" \
  -no-window -no-snapshot -no-audio > /dev/null 2>&1 &
adb -s "emulator-$CONSOLE_PORT" wait-for-device
until [ "$(adb -s "emulator-$CONSOLE_PORT" shell getprop sys.boot_completed | tr -d '\r')" = 1 ]; do sleep 5; done
```

Always pass `-s emulator-<port>`. Even a private adb server finds the user's emulators on the usual
ports, and without `-s` an `adb install` fails on "more than one device", or lands on the wrong one.
Drop `-no-window` when the user wants to watch. Add `-gpu host` for anything that measures frames
— the macrobenchmarks of the `baseline-profile` skill — since a headless emulator otherwise
renders in software and the numbers measure that instead of the app.

## Run tests on it

Gradle's `connected*` tasks go through the default adb server and install on every device, so don't
use them here. Build the APKs, install them on this emulator, and run `am instrument`:

```bash
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest
adb -s "emulator-$CONSOLE_PORT" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "emulator-$CONSOLE_PORT" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s "emulator-$CONSOLE_PORT" shell pm clear com.pierre.tunescout
adb -s "emulator-$CONSOLE_PORT" shell am instrument -w \
  -e runnerBuilder de.mannodermaus.junit5.AndroidJUnit5Builder \
  -e class com.pierre.tunescout.<TestClass> \
  com.pierre.tunescout.test/androidx.test.runner.AndroidJUnitRunner
```

- `am instrument` doesn't go through the Orchestrator, so nothing clears the app's data for it. Run
  one class at a time, with a `pm clear` before each.
- A landscape run only stays in landscape with the rotation locked. `settings put system
  user_rotation` is undone as soon as a UiAutomation client disconnects:

  ```bash
  adb -s "emulator-$CONSOLE_PORT" shell cmd window user-rotation lock 1   # 0 for portrait
  adb -s "emulator-$CONSOLE_PORT" shell cmd window fixed-to-user-rotation enabled
  adb -s "emulator-$CONSOLE_PORT" shell dumpsys window | grep -m1 -o 'ROTATION_[0-9]*'
  ```

## Delete

`finish-task` does this once the branch's merge is verified. Shut the emulator down if it is
running, wait for it to exit, then remove its two paths:

```bash
AVD=TuneScout_<short_description>
pkill -f -- "-avd $AVD( |$)"
while pgrep -f -- "-avd $AVD( |$)" > /dev/null; do sleep 1; done
rm -rf ~/.android/avd/"$AVD".avd ~/.android/avd/"$AVD".ini
```

- Never delete an AVD that isn't named after the task being finished. `Medium_Phone`, `Pixel_9`,
  `Pixel_Tablet` and `TuneScout_Benchmark` are shared.
- If the task never created one (started before this step existed, or the user skipped it), there
  is nothing to delete, so move on.
