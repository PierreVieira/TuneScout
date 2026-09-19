# README screenshots

The images in the [README](../README.md) are **generated, not captured**. Each one renders a real
`*Content` composable under Robolectric, inside a phone mockup drawn by the
[store-screenshots](https://github.com/lucianosantosdev/store-screenshots) library, under a title
and a one-line description, and is written to `docs/screenshots/`. That folder is versioned — a
reader has to see the app without running anything — so regenerating it is part of the change that
moved the screen.

No store is involved: the app is not published, and these images exist only for the README.

## Regenerating

```bash
./scripts/screenshots.sh
```

or, the same thing without the wrapper:

```bash
./gradlew updateReadmeScreenshots
```

It runs the generators in `:tools:screenshots` (about 30 seconds, no device and no emulator),
scales each 1242x2484 PNG down to 520px wide and rewrites `docs/screenshots/`. Review the diff and
commit it with the change that caused it.

The task **deletes every PNG in `docs/screenshots/` except the manual ones** before writing, so a
shot that was renamed or dropped cannot stay in the folder — and in the README — forever.

## What the images show

| File | Generator | Title | State |
|---|---|---|---|
| `splash.png` | `SplashScreenshots` | Straight into the music | |
| `songs.png` | `SongsScreenshots.songs` | Pick up where you left off | Recently played, five songs, under a mini player holding a restored, paused song. |
| `search.png` | `SongsScreenshots.search` | Find any song as you type | The query `daft punk` over ten paged results. |
| `player.png` | `PlayerScreenshots` | A player, and the queue behind it | Get Lucky, playing, 18 seconds into a 29-second preview. |
| `queue.png` | `QueueScreenshots` | Queue what you want next | The queue sheet over the player: Random Access Memories playing, two songs queued by hand ahead of it. |
| `options.png` | `SongOptionsScreenshots` | Queue it, or open its album | The song options sheet over the player. |
| `album.png` | `AlbumScreenshots` | The album behind the song | Random Access Memories, loaded, with both queue actions in the top bar. |
| `theme.png` | `ThemeSelectionScreenshots` | Light, dark, or whatever the phone says | The theme sheet over the songs screen, dark selected, dynamic colors off. |
| `library.png` | `LibraryScreenshots.library` | Keep what you like | The library tab as a list: liked songs, three playlists, one of them still empty. |
| `library_grid.png` | `LibraryScreenshots.libraryGrid` | Or see them as covers | The same library in the grid view. |
| `notification.png` | — | — | **Manual capture**, captioned in the README itself. |

Every generator renders under `Theme.DARK` rather than the default `Theme.SYSTEM`: Robolectric
reports a light system theme, so leaving it to the default would silently flip every image the
first time the images are regenerated.

The copy is English only — the file is committed once and read in one language, so the generators
do not loop over locales. A title is a benefit, not a screen name ("Pick up where you left off",
not "Songs"), and the description underneath says how the app delivers it in one line. Both live
next to the screen's `capture` call; changing one means changing the table above with it.

The two library shots pass `LibraryContent` its view mode directly, which is also how the screen
gets it from the DataStore.

`queue.png`, `options.png` and `theme.png` all draw their sheet by hand — a `Surface` with the drag handle over a
scrim — because `ModalBottomSheet` animates in and Robolectric captures the frame before it lands.

The `songs.png` shot is the one that composes two screens: `SongsContent` under
`MiniPlayerContent`, which is how `app` lays them out. The mini player lives outside the
`NavDisplay`, so no single `*Content` composable carries it.

`notification.png` is the one image that is not generated: the media controls live in the system
notification shade, which is not a composable and cannot be rendered by a Compose test. It is a
device capture, committed once, and `updateReadmeScreenshots` leaves it alone — the file is listed
in `manualScreenshots` in the root `build.gradle.kts`.

## Where to edit

Everything lives in one module, `tools/screenshots/src/test/kotlin/.../screenshots/`:

```
ReadmeScreenshotsTest.kt   # base class: phone form factor, canvas background, theme, Coil setup
ScreenshotFixtures.kt      # the songs and the album every shot renders
ScreenshotArtwork.kt       # the fake Coil loader that serves the committed covers
<Screen>Screenshots.kt     # one class per screen: the UiState it renders and the file name
```

Adding a screen means one new `<Screen>Screenshots.kt` and, if the screen lives in a module the
test classpath does not have yet, one `testImplementation` line in
`tools/screenshots/build.gradle.kts`. The file name passed to `capture` is what lands in
`docs/screenshots/`, so it is also what the README references.

## The pieces that are not obvious

**The generators are the only JUnit 4 in the project.** `StoreScreenshotsTest` is a Robolectric
(JUnit 4) base class, while every other test here is JUnit 6 Jupiter. That is why
`:tools:screenshots` is a module of its own and the only one with `junit-vintage-engine` on its
test runtime: the feature modules stay Jupiter-only. `testDebugUnitTest` runs the generators in CI
too, so a screen that stops rendering fails the build like any other test.

**Artwork is served from the repository, not the network.** Robolectric has no network, so the
three album covers under `src/test/resources/artwork/` are handed to Coil by a
`FakeImageLoaderEngine` installed as the singleton loader. A fixture's `Artwork` URL points at
`https://artwork.tunescout.test/<album>/100x100bb.jpg`, and the engine matches on the album segment
— which means the real `Artwork.thumbnailUrl` / `mediumUrl` / `largeUrl` resizing still runs, and
every surface still asks for the size it renders. An artwork URL the engine does not recognise
fails the test instead of rendering a blank square.

**The banner sets the canvas, the mockup gets what is left.** The title and description are drawn
at the top of the 1242x2484 canvas by the library's own frame, and the phone takes the remaining
height, over the gradient in `ReadmeScreenshotsTest`. That gradient is deliberately lighter than
both the app's black background and its teal splash — on a darker canvas the splash screen blends
into it and the device disappears. The 520px render width and the 270px the README asks for are a
pair: below roughly that size the description stops being readable, which is why the grid is three
across rather than seven.

**`edgeToEdge = false`.** The screens pad themselves with `safeDrawingPadding`, which measures zero
in a Compose test with no real window, so the frame reserves the status bar's height instead. That
is also what keeps the frame's own clock from sitting on top of the "Songs" title.

**The options sheet is assembled here.** The real sheet is a `ModalBottomSheet` opened by
`BottomSheetSceneStrategy`, which renders in a separate window that a Robolectric capture does not
see. `SongOptionsScreenshots` stacks the same parts by hand — scrim, drag handle, sheet colour —
around the real `SongOptionsContent`.

**Images are scaled in halving steps.** `updateReadmeScreenshots` halves the 1242px render until
it is close to the 520px target instead of drawing straight down to it; a single draw skips too
many pixels and leaves the app's hairline dividers and small text ragged.
