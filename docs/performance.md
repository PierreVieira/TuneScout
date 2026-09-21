# Performance

The app was built to be fast: search is paged and debounced, artwork and previews are cached, R8
shrinks the release build, and UI states are shaped so a screen only recomposes what changed. This
page is where that becomes numbers: a Baseline Profile that ships with the app, the macrobenchmarks
that measure what it buys, and what the Compose compiler says about stability.

## Baseline Profile

`:app` ships a Baseline Profile and a startup profile, committed under
[`app/src/main/generated/baselineProfiles`](../app/src/main/generated/baselineProfiles):

- **`baseline-prof.txt`** lists the classes and methods the critical journeys run. The install
  compiles them ahead of time, so they do not start out interpreted and wait for the JIT.
  `profileinstaller` installs it on devices where the Play Store did not, including a sideloaded APK.
- **`startup-prof.txt`** lists what the cold start alone runs. R8 uses it to put those classes in
  the primary dex file (`dexLayoutOptimization`), so starting the app reads fewer pages.

Both come from [`BaselineProfileGenerator`](../tools/baselineprofile/src/main/kotlin/com/pierre/tunescout/baselineprofile/BaselineProfileGenerator.kt)
in `:tools:baselineprofile`, which drives a release-like build (minified off so the rules keep their
real names, not debuggable) through these journeys with UiAutomator:

1. Cold start through the splash to the home screen (the only journey in the startup profile)
2. Search for "daft punk" and fling the results, loading more pages
3. Play the first result, open the player from the mini player and close it
4. Open that song's album from its options sheet and fling its tracks

The journeys live in [`Journeys.kt`](../tools/baselineprofile/src/main/kotlin/com/pierre/tunescout/baselineprofile/Journeys.kt),
shared by the generator and the benchmarks. The app has no test hooks for them: they find screens
by what is drawn — the English strings and content descriptions — and by the two lists tagged for
it (`search_results`, `album_tracks`). `MainContent` sets `testTagsAsResourceId` so UiAutomator can
see those tags, in every build type.

## Measured

Each journey is measured twice: with `CompilationMode.None()`, which drops every compiled method and
runs the app as a fresh install without a profile would, and with
`CompilationMode.Partial(BaselineProfileMode.Require)`, which installs the profile first. Ten
iterations each.

**Device:** the `TuneScout_Benchmark` emulator — a Pixel 9 profile running Android 16 (API 36.1,
Google Play arm64 image), 4 cores and 2 GB of RAM, on an Apple M4 Max. Two more emulators were
running on the same host. An emulator is not a phone: take the numbers as a comparison between the
two columns, not as what a user feels. Measured on 2026-09-21.

### Cold start

`StartupTimingMetric`, time to initial display, from the launcher to the home screen's first frame
past the splash.

| | Without profile | With profile | Change |
|---|--:|--:|--:|
| Min | 260.6 ms | 269.0 ms | |
| **Median** | **326.7 ms** | **288.6 ms** | **−11.7%** |
| Max | 372.9 ms | 310.1 ms | −16.8% |

The profile moves the median and, more, the tail: the slowest start without it is 63 ms slower than
the slowest one with it.

### Frames

`FrameTimingMetric`. `frameDurationCpuMs` is the CPU time a frame took; `frameOverrunMs` is how late
it was for its deadline, so anything above zero is a frame the user saw drop. The device refreshes
at 60 Hz, a 16.7 ms budget.

**Scrolling search results** (three flings down, paging in more results, and three back up):

| | Without profile | With profile |
|---|--:|--:|
| CPU time P50 / P90 / P99 | 7.5 / 18.2 / 34.4 ms | 6.5 / 7.5 / 22.0 ms |
| Overrun P50 / P90 / P99 | −3.7 / 3.1 / 21.8 ms | −4.6 / −1.4 / 7.3 ms |

**Scrolling an album's tracks:**

| | Without profile | With profile |
|---|--:|--:|
| CPU time P50 / P90 / P99 | 7.2 / 24.8 / 48.9 ms | 6.9 / 17.5 / 24.3 ms |
| Overrun P50 / P90 / P99 | 0.1 / 12.9 / 41.7 ms | −0.2 / 2.5 / 9.5 ms |

The median frame barely moves — it is already cheap — but the slow frames do: without the profile,
the first rows of each kind are drawn by interpreted code while the JIT catches up. At P90 the
search results go from dropping frames to not dropping them, and the worst album frames are late by
a quarter of what they were.

**Opening the player** from the mini player — the shared artwork transition — and closing it:

| | Without profile | With profile |
|---|--:|--:|
| CPU time P50 / P90 / P99 | 32.3 / 37.5 / 47.6 ms | 32.3 / 35.0 / 46.1 ms |
| Overrun P50 / P90 / P99 | 18.5 / 31.0 / 48.3 ms | 18.2 / 28.9 / 45.8 ms |

This is the finding the benchmarks turned up: the player costs about 32 ms of CPU a frame with or
without the profile, so it is not code waiting to be compiled but work done on every frame while
the player is on screen — on this emulator, every frame of it is dropped. It is left for its own
change, which should start from a trace of this benchmark (every iteration writes a Perfetto trace
next to the results).

## Compose stability

The Compose compiler reports which classes it considers stable and which composables can skip.
They are off by default; `-Ptunescout.composeReports=true` turns them on, and each module writes
them to `build/compose_compiler`. A compile restored from the build cache writes no report, so
force the compile:

```bash
./gradlew compileReleaseKotlin --rerun-tasks --no-build-cache -Ptunescout.composeReports=true
```

**What it flagged.** Strong skipping is on, so every restartable composable is already skippable —
but an unstable parameter is compared by instance, not by value. `core:model` is a plain JVM module
the compiler never sees, so every `Song`, `Album`, `NowPlaying`, `QueueEntry` and `Playlist` was
unstable, and with them every UI state holding one: the search results, recently played, the
player, the mini player, the album, the queue. A new `Song` equal to the last one — which is what
every Room emission is — recomposed the row, the mini player or the album header that took it.

**The fix** is [`compose_stability.conf`](../compose_stability.conf), which every Compose module
reads through `configureComposeCompiler` in build-logic. It declares `core.model.**` stable —
data classes, enums and value classes of `val`s — and `List` and `Set`, since every collection that
reaches a UI state is built by a map, a filter or a query and never mutated after. `ImmutableList`
would say the same thing in the type, at the cost of a dependency and a conversion at every
boundary, for no difference to the compiler.

**What is still flagged, and why that is fine:**

| Where | Parameter | Why it stays |
|---|---|---|
| `ErrorMessage` (songs) | `error: Throwable` | Only drawn in the error state, which does not recompose on its own |
| `sharedArtwork`, `sharedTextBounds`, `SeekBar` (ui) | `key: Any?`, `contentKey: Any` | Keys compared by `equals`; the callers pass a `Long` or a data class |
| The widgets | `Bitmap` | Glance composes a widget once per update into RemoteViews; there is no recomposition to skip |
| `MainActivity.ThemedContent` | `this` | A member of the activity, recomposed only when the whole state changes |
| `MainUiState.Ready`, `ThemeSelectionUiState`, the library's click events | *runtime* | Stable when the instances inside are, which they are; checked when composed, not at compile time |

## Reproducing

Both need a device or an emulator on API 28 or newer, in English, with network access. Neither
runs on CI: they take tens of minutes and need a device nothing else is using. Another checkout
running its connected tests at the same time installs its own build over the app and fails the run
— `connectedAndroidTest` without `ANDROID_SERIAL` uses every connected device.

**Regenerate the profile** after a change to a journey or to the code it runs, and commit what it
writes. The two files run to tens of thousands of lines; `.gitattributes` marks them generated, so
a pull request's diff collapses them:

```bash
ANDROID_SERIAL=<device> ./gradlew :app:generateBaselineProfile
```

**Run the benchmarks.** On an emulator, the benchmark library refuses to run unless told the error
is expected:

```bash
ANDROID_SERIAL=<device> ./gradlew :tools:baselineprofile:connectedBenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
```

The results print to the console and land in
`tools/baselineprofile/build/outputs/connected_android_test_additional_output`, with a Perfetto trace
per iteration. A single class runs with
`-Pandroid.testInstrumentationRunnerArguments.class=com.pierre.tunescout.baselineprofile.StartupBenchmark`.

What each benchmark does to reach its screen:

- `StartupBenchmark` uses `StartupMode.COLD`: the app is killed before each iteration.
- `ScrollBenchmark` kills the app itself at the start of each setup. `StartupMode.COLD` would kill
  it after the setup, and the measured block would find nothing on screen.
- `PlayerTransitionBenchmark` clears the app's data at the start of each setup. Otherwise the cold
  start restores the last session, the result it taps is already the current song, and tapping the
  current song opens the player from the list instead of starting it.
