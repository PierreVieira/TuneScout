---
name: baseline-profile
description: "Run the macrobenchmarks of :tools:baseline_profile and regenerate the app's Baseline Profile on the task's own emulator, then read the numbers and the Perfetto traces."
---

# Baseline Profile

`:tools:baseline_profile` holds the profile generator and the three macrobenchmarks — cold start,
scrolling the two long lists, and the player transition — that
[docs/performance.md](../../../docs/performance.md) reports. Nothing in it runs on CI; this skill
is how it runs by hand, on a device nobody else is using.

The device is the task's emulator from [`task-emulator.md`](../task-emulator.md), on its private
adb server. Gradle's `connected*` tasks (`:app:generateBaselineProfile`,
`:tools:baseline_profile:connectedBenchmarkReleaseAndroidTest`) cannot reach it: AGP's runner only
talks to the default adb server, where it would install on every emulator it sees, including the
ones other sessions are driving. So everything here builds the APKs with Gradle, installs them with
`adb -s`, and runs `am instrument` directly. The only Gradle task that touches the profile
afterwards is the merge, which needs no device.

## Step-by-step workflow

### 1. Boot the task's emulator

Follow the "Boot" section of [`task-emulator.md`](../task-emulator.md), adding `-gpu host` to
the emulator's arguments. Three things matter more here than for a flow test:

- **Render with the host's GPU.** A headless emulator (`-no-window`) with the AVD's default
  `hw.gpu.mode=auto` picks SwiftShader, a software renderer, and every frame metric then measures
  it: the render thread spends 25–35 ms a frame drawing while the app's own work is one or two.
  `-gpu host` keeps the real GPU without a window. Check the emulator's log before trusting a
  number — `GPU Renderer=[Android Emulator OpenGL ES Translator (Apple M4 Max)]` is right;
  anything naming SwiftShader is not.
- **Export `ANDROID_ADB_SERVER_PORT` in the same shell before launching the emulator.** The
  emulator registers with whichever adb server the variable names at launch, and nothing moves it
  afterwards. Launched without it, it lands on the default server, every session sees it, and
  `adb -s` on the private port says `device not found`.
- **Wait for the CPU to go idle, not only for `sys.boot_completed`.** A fresh AVD spends about two
  minutes after its first boot on Play Services and the keyboard, and any benchmark iteration in
  that window measures them. Poll `top` until it reports the CPU mostly idle twice in a row:

  ```bash
  export ANDROID_ADB_SERVER_PORT=<console port + 1000>
  S=emulator-<console port>
  adb -s $S shell top -n 1 -b | grep -m1 -o '[0-9]*%idle'   # 4 cores: 400%cpu, so idle ≥ 320% is quiet
  ```

The journeys read the app's English strings, so leave the emulator in English (the default), and
in portrait: `adb -s $S shell cmd window user-rotation lock 0`.

Note what else the host is running (`top -l 2 -o cpu`), and say so with the numbers: another
session's emulator at 200–300% CPU moves every percentile, and the docs' tables name what ran
beside them for that reason.

### 2. Build the APKs

The benchmarks measure the `benchmarkRelease` build: minified, not debuggable, with the committed
profile inside it. The generator drives `nonMinifiedRelease`, so the rules it writes carry real
names. Build only what the task needs — each pair takes a few minutes:

```bash
./gradlew :app:assembleBenchmarkRelease :tools:baseline_profile:assembleBenchmarkRelease
./gradlew :app:assembleNonMinifiedRelease :tools:baseline_profile:assembleNonMinifiedRelease
```

Both `:app` APKs are signed with the debug key locally, so they install over each other.

### 3. Run the benchmarks

Install the app and the test APK (`-t`, since it is a test package), then run **one class at a
time** with `-e class`. Without a class the whole package runs, generator included, and the
generator's run would then be mistaken for a measurement:

```bash
WT=<worktree root>
adb -s $S install -r -t $WT/app/build/outputs/apk/benchmarkRelease/app-benchmarkRelease.apk
adb -s $S install -r -t $WT/tools/baseline_profile/build/outputs/apk/benchmarkRelease/baseline_profile-benchmarkRelease.apk
adb -s $S shell am instrument -w -r \
  -e androidx.benchmark.suppressErrors EMULATOR \
  -e class com.pierre.tunescout.baselineprofile.StartupBenchmark \
  com.pierre.tunescout.baselineprofile/androidx.test.runner.AndroidJUnitRunner
```

`suppressErrors EMULATOR` is what lets the benchmark library run on an emulator at all. The
classes are `StartupBenchmark` (about a minute), `ScrollBenchmark` and `PlayerTransitionBenchmark`
(several minutes each: ten iterations of a search and a scroll, or of a `pm clear` and a cold
start). Run them in the background and watch the log for `INSTRUMENTATION_STATUS: test=`, which
marks each test starting, and `INSTRUMENTATION_CODE: -1`, which marks the class finishing.

Each test prints its result once it finishes:

```
timeToInitialDisplayMs   [min 328.0](file://...iter007...perfetto-trace),   [median 355.1](...),   [max 477.7](...)
frameDurationCpuMs   P50  6.5,   P90  7.5,   P95  ...,   P99  22.0
frameOverrunMs       P50 -4.6,   P90 -1.4,   P95  ...,   P99  7.3
```

Everything it wrote — one Perfetto trace per iteration, and
`com.pierre.tunescout.baselineprofile-benchmarkData.json` with every iteration's numbers — lands
on the device in `/storage/emulated/0/Android/media/com.pierre.tunescout.baselineprofile/`.
**The next `am instrument` run deletes all of it**, the generator's included, so pull the
directory into the module's build folder (git-ignored, and where the Gradle path would have put
it) right after each class, under a name of its own:

```bash
adb -s $S pull /storage/emulated/0/Android/media/com.pierre.tunescout.baselineprofile \
  $WT/tools/baseline_profile/build/outputs/connected_android_test_additional_output/<class>/
```

### 4. Read the traces

Macrobenchmark leaves `trace_processor_shell` on the device, so no local Perfetto install is
needed. Push a query and run it over a trace; the app's own sections show up as slices on its
main thread and `RenderThread`:

```bash
adb -s $S push query.sql /data/local/tmp/query.sql
adb -s $S shell /data/local/tmp/trace_processor_shell -q /data/local/tmp/query.sql \
  "/storage/emulated/0/Android/media/com.pierre.tunescout.baselineprofile/<trace>.perfetto-trace"
```

Drive that from a bash script rather than an inline zsh command, which does not word-split
variables holding a command. Queries that have paid off:

- **Where a cold start goes** — the main-thread slices between `bindApplication` and the first
  `Choreographer#doFrame`, ordered by duration, tell whether the time is class loading (`L…;`
  slices under the `dalvik` category), the Koin graph, or the first composition.
- **What a slow frame did** — `RenderThread` slices named `flush layers` are offscreen layers
  (anything with alpha below 1 over overlapping content); `dequeueBuffer` is the emulator's
  display back-pressure, not the app.
- **Whether the iterations are comparable** — the iterations of a frame benchmark run for
  different lengths, because UiAutomator waits while the home screen keeps animating. Compare
  per-iteration sums, not only P50.

Read the numbers against the tables in [docs/performance.md](../../../docs/performance.md): same
journeys, same metrics. A P50 that moved by a few milliseconds is noise on an emulator; a P90 or
P99 that doubled, or a profile that stopped changing a number, is a finding.

### 5. Regenerate the profile

Regenerate after a change to a journey, or to code the journeys run — the profile only lists what
it saw, and a stale one loses the rest silently. Install the `nonMinifiedRelease` pair and run the
generator class the same way as a benchmark:

```bash
adb -s $S install -r -t $WT/app/build/outputs/apk/nonMinifiedRelease/app-nonMinifiedRelease.apk
adb -s $S install -r -t $WT/tools/baseline_profile/build/outputs/apk/nonMinifiedRelease/baseline_profile-nonMinifiedRelease.apk
adb -s $S shell am instrument -w -r \
  -e androidx.benchmark.suppressErrors EMULATOR \
  -e class com.pierre.tunescout.baselineprofile.BaselineProfileGenerator \
  com.pierre.tunescout.baselineprofile/androidx.test.runner.AndroidJUnitRunner
```

`BaselineProfileRule` runs each test's journeys again and again until the profile stops changing,
so this takes about twenty minutes, most of it the `criticalJourneys` test; the `waitForIdle`
timeouts UiAutomator logs meanwhile are the home screen animating, not a failure. It writes
`…_criticalJourneys-baseline-prof-….txt` and `…_startup-startup-prof-….txt` in the same media
directory (each twice, once with a timestamp). The Gradle plugin's `collect` task would copy them out of the runner's
`test-result.pb`, which `am instrument` never writes, so put them where `collect` would have and
run the merge without the two device tasks:

```bash
DEST=$WT/tools/baseline_profile/build/intermediates/baselineprofiles/nonMinifiedRelease
rm -rf "$DEST" && mkdir -p "$DEST"
adb -s $S shell 'ls /storage/emulated/0/Android/media/com.pierre.tunescout.baselineprofile/*-prof-*.txt' \
  | tr -d '\r' | while read -r f; do adb -s $S pull "$f" "$DEST/"; done
./gradlew :app:generateBaselineProfile \
  -x :tools:baseline_profile:connectedNonMinifiedReleaseAndroidTest \
  -x :tools:baseline_profile:collectNonMinifiedReleaseBaselineProfile
```

The merge rewrites [`app/src/main/generated/baselineProfiles`](../../../app/src/main/generated/baselineProfiles):
`baseline-prof.txt` from every test, `startup-prof.txt` from the startup test alone. Check
`git diff --stat` on them — tens of thousands of lines each, marked generated in `.gitattributes` —
and commit them with the change that made them stale.

### 6. Report, and update the docs when the numbers move

Report the tables the way `docs/performance.md` does: without and with the profile, min/median/max
for the start, P50/P90/P99 of `frameDurationCpuMs` and `frameOverrunMs` for the frames. When a
change is meant to move them, update that page's tables, its "Measured on" date and its device
line, and say what was running on the host alongside — two more emulators change the numbers.

Shut the emulator down when done; `finish-task` deletes the AVD.

## Edge cases

- **`device 'emulator-<port>' not found` on the private server** — the emulator was launched
  without `ANDROID_ADB_SERVER_PORT` exported. Kill it and relaunch; `adb connect` would only add a
  second name for a device the default server still shares.
- **`Nothing on screen matched …`** — a journey could not find its screen. The emulator is not in
  English, a label the journeys read was renamed (see `Journeys.kt`), or a notification or ANR
  dialog is covering the app (the CPU was not idle yet). Not a performance finding.
- **`Benchmark… is debuggable`** — the debug APK is installed. Only `benchmarkRelease` and
  `nonMinifiedRelease` measure.
- **The app was replaced mid-run** — another session ran a `connected*` task against the default
  server and the emulator was visible to it. Iterations after that point measure their build.
- **`Requested adb port (…) is outside the recommended range`** on the emulator's console — that is
  the port the task emulator asks for on purpose; it only means the default server will not scan it.
- **`Expected test results were not found`** from `generateBaselineProfile` — the `collect` task
  ran. Both `-x` flags are needed.
