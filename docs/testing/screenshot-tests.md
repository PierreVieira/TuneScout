# Screenshot tests

Every screen is rendered on the JVM and compared, pixel by pixel, against an image committed to the
repository. A layout that shifts, a colour that changes, a string that starts to truncate: the build
fails and the difference is an image a reviewer can look at.

The tests live in `:tools:screenshot_tests` and run under Robolectric with
[Roborazzi](https://github.com/takahirom/roborazzi) — no device and no emulator, like the README
generators in [`:tools:screenshots`](../screenshots.md), with which they share their fixtures.

## Running them

```bash
./gradlew :tools:screenshot_tests:verifyRoborazziDebug
```

Verifying locally on macOS **fails**: the references are rendered on Linux, and the same screen comes
out a few pixels different on another platform. Locally the useful run is the plain one, which
renders every screen and catches a screen that stops composing without comparing anything:

```bash
./gradlew :tools:screenshot_tests:testDebugUnitTest
```

CI verifies on every pull request and uploads `screenshot-differences` when it fails — one
`<name>_compare.png` per failure, holding the reference, the new render and the difference between
them side by side.

## Changing a screen on purpose

The references are part of the change, but they cannot be recorded on a developer's machine. Label
the pull request **`record-screenshots`**: the `screenshot-tests` workflow renders them on Linux,
commits them to the branch and drops the label, so the next push verifies again.

`recordRoborazziDebug` is what that workflow runs, and it is also how a new test's first images are
produced — on CI, through the label.

Update the branch with `main` before asking for the recording. A pull request is verified against
its **merge** with `main`, while the references are recorded on the branch alone: a screen `main`
changed in the meantime would be recorded as the branch still draws it and fail verification right
after.

## Adding a test

One class per screen, extending `ScreenshotTest`, one `@Test` per state worth looking at:

```kotlin
internal class AlbumScreenshotTest : ScreenshotTest() {
    @Test
    fun loadedStale() {
        snapshot(name = "loaded_stale") {
            AlbumContent(uiState = loaded.copy(isStale = true), isHeaderInline = false, onEvent = {})
        }
    }
}
```

`snapshot` renders the composable once per [variant](#variants) into
`src/test/screenshots/<test class>/<name>_<variant>.png`, which is the file a reviewer opens. It
takes the stateless `*Content` composable and a `UiState`, the same pair the instrumented screen
tests use, so a state that cannot be written down cannot be captured either.

A screen in a module the test classpath does not have yet needs one `testImplementation` line in
`tools/screenshot_tests/build.gradle.kts`.

### Variants

| Variant | What it renders |
| --- | --- |
| `LIGHT`, `DARK` | the default for every snapshot, since most regressions are a colour that reads in one theme and not the other |
| `LARGE_FONT_PT_BR` | Portuguese copy (longer than English) at a 1.5x font scale — the combination that truncates or wraps first |
| `LARGEST_FONT` | English at a 2x font scale, the largest the system's font size setting reaches — where a fixed height clips and a row stops fitting its controls |

The last two are deliberately not the default: passing `variants = ScreenshotVariant.all` on the
states that carry the most text says where truncation matters, instead of multiplying every image in
the repository. `isLandscape = true` covers the layouts that switch on width (`isSideBySide`,
`isHeaderInline`).

### What is covered

| Test | States |
| --- | --- |
| `PlayerScreenshotTest` | loading, not found, loaded, ended repeating the song, shuffled repeating the queue, side by side |
| `AlbumScreenshotTest` | loading, error, loaded, playing, unplayable tracks, stale, inline header |
| `SongsScreenshotTest` | recently played, empty, searching, searching scrolled under the header's shadow, no results, offline with unplayable rows, unavailable search results, remove confirmation, inline header, no audio search |
| `LibraryScreenshotTest` | list, grid, filtered, empty, search: recent, results and no results |
| `CollectionScreenshotTest` | loading, loaded, favourites, empty, remove confirmation |
| `QueueScreenshotTest` | playing, paused, context only, empty |
| `MiniPlayerScreenshotTest` | playing, paused, ended |
| `AudioSearchScreenshotTest` | waiting for speech, hearing with a transcript, failed |
| `ThemeSelectionScreenshotTest` | the three themes, dynamic colour on, dynamic colour unsupported, its info dialog |
| `ListDetailScreenshotTest` | the songs beside an album: recently played, and search results in every variant |
| `ComponentScreenshotTest` | the `:ui:component` pieces on their own: rows and skeletons, bars and inputs, play button, state messages, name prompt, confirmation dialog |

The components have a test of their own on purpose: a change to `SongRow` shows up as one diff
instead of the same diff repeated across every screen that draws it.

Left out: the splash screen (one static gradient), the options sheets (`OptionsSheet` is covered by
its own components) and anything that only exists mid-animation.

## Accessibility checks

Every capture is also handed to the
[Accessibility Test Framework](https://github.com/google/Accessibility-Test-Framework-for-Android)
through Roborazzi's `checkRoboAccessibility` — the same checks Accessibility Scanner runs on a
device: touch targets under 48dp, text and image contrast, items with no label, clickable items that
share one. It runs on each root, so a dialog is checked as well as the screen under it.

An **error** fails the test. **Warnings** are printed and do not: they include measurements no
screen can settle — contrast is read off the rendered pixels, so text over artwork, or the rows
faded on purpose because they cannot play, are reported every time. Read them in the test's
standard error when touching colours; the palette itself is held to 4.5:1 by
`TuneScoutPalettesContrastTest`.

This needs no device and no reference image, so unlike the comparison it also runs locally:

```bash
./gradlew :tools:screenshot_tests:testDebugUnitTest
```

## The pieces that are not obvious

**A capture owns its Compose rule.** `ScreenshotTest.capture` creates a `createComposeRule()` per
image and evaluates it by hand. The rule is what holds the single `setContent`, and it is also what
keeps the endless animations — the marquee under a long title, the shimmer in a skeleton, the now
playing bars — from spinning frames forever instead of settling into a frame to photograph.
`captureRoboImage { }` without a rule hangs on any screen that animates.

**Artwork comes from the repository.** Robolectric has no network, so the covers under
`tools/screenshot_fixtures/src/main/resources/artwork/` are served to Coil by a fake engine. This is
the same `createArtworkImageLoader` the README generators install; the songs, the album and the
sheet frame around a `*Content` live in `:tools:screenshot_fixtures` so both suites share one set.

**The theme is pinned per variant.** Robolectric reports a light system theme, so `Theme.SYSTEM`
would silently flip every dark image the first time they were recorded.

**The images are half size.** `roborazzi.record.resizeScale=0.5` halves the 1078x2399 the device
renders. A layout, a colour or a truncated string is as visible at 539 pixels wide, in a quarter of
the bytes — and these files are versioned.

**Robolectric runs on SDK 36, not the compileSdk.** Booting 37 needs extra `--add-opens` JDK flags,
and nothing the app draws differs between the two. SDK 36 itself needs
`--add-exports=java.base/jdk.internal.access=ALL-UNNAMED`, which the module's test task passes:
Android 16 reaches `FileDescriptor`'s private fields through `jdk.internal`.

**JUnit 4 lives here, and only here.** Roborazzi needs Robolectric, which is a JUnit 4 runner, so
this module and `:tools:screenshots` are the only ones with the vintage engine on their test
runtime — the feature modules stay Jupiter-only. See [the testing guide](README.md).
