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
scales each 1242x2484 phone PNG down to 520px wide, the two 2560x1600 tablet PNGs down to 1600px
wide, and rewrites `docs/screenshots/`. Review the diff and commit it with the change that caused
it.

The task **deletes every PNG in `docs/screenshots/` except the five manual ones** before writing, so a
shot that was renamed or dropped cannot stay in the folder — and in the README — forever.

## What the images show

| File | Generator | Title | State |
|---|---|---|---|
| `splash.png` | `SplashScreenshots` | Straight into the music | The splash as it leaves, with the gradient fully in. Not a composable: see below. |
| `songs.png` | `SongsScreenshots.songs` | Pick up where you left off | Recently played, five songs, under a mini player holding a restored, paused song. |
| `search.png` | `SongsScreenshots.search` | Find any song as you type | The query `daft punk` over ten paged results. |
| `audio_search.png` | `AudioSearchScreenshots` | Or just say it | The audio search sheet over the songs screen, mid-sentence: `Daft Punk get lucky` heard so far, with the halos wide open. |
| `player.png` | `PlayerScreenshots` | A player, and the queue behind it | Get Lucky, playing and liked, 18 seconds into a 29-second preview. |
| `queue.png` | `QueueScreenshots` | Queue what you want next | The queue sheet over the player: Random Access Memories playing, two songs queued by hand ahead of it, and the clear button beside the title. |
| `options.png` | `SongOptionsScreenshots` | Queue it, or open its album | The song options sheet over the player, liking left out since the player's own bar already carries it. |
| `album.png` | `AlbumScreenshots` | The album behind the song | Random Access Memories, loaded and liked, with the overflow beside the heart and on every track. |
| `theme.png` | `ThemeSelectionScreenshots` | Light, dark, or whatever the phone says | The theme sheet over the songs screen, dark selected, dynamic colors off. |
| `library.png` | `LibraryScreenshots.library` | Keep what you like | The library tab as a list: liked songs, three playlists (one still empty) and a liked album, with no filter chip picked. |
| `library_grid.png` | `LibraryScreenshots.libraryGrid` | Or see them as covers | The same library in the grid view. |
| `two_pane_songs.png` | `TwoPaneScreenshots.songsBesidePlayer` | One breakpoint, one more pane | A landscape tablet, past the 800dp two-pane breakpoint: the songs list beside the player it opened, Get Lucky playing. |
| `two_pane_library.png` | `TwoPaneScreenshots.libraryBesideAlbum` | The same pane, every detail | The same tablet: the library beside Random Access Memories, opened from it. |
| `widget_shortcuts.png` | — | — | **Manual capture**: the 4×2 widget, now playing with the five songs played last, cropped out of the home screen. |
| `widget_now_playing.png` | — | — | **Manual capture**: the 4×1 widget, from the same home screen capture. |
| `lock_screen.png` | — | — | **Manual capture**: the media controls on the locked screen. |
| `lock_screen_expanded.png` | — | — | **Manual capture**: the same controls after a tap on them, expanded to the cover, the timeline and the like button. |
| `notification.png` | — | — | **Manual capture**: the media controls in the notification shade, like button included. |

Every generator renders under `Theme.DARK` rather than the default `Theme.SYSTEM`: Robolectric
reports a light system theme, so leaving it to the default would silently flip every image the
first time the images are regenerated.

The copy is English only — the file is committed once and read in one language, so the generators
do not loop over locales. A title is a benefit, not a screen name ("Pick up where you left off",
not "Songs"), and the description underneath says how the app delivers it in one line. Both live
next to the screen's `capture` call; changing one means changing the table above with it.

The two library shots pass `LibraryContent` its view mode directly, which is also how the screen
gets it from the DataStore.

`queue.png`, `options.png`, `theme.png` and `audio_search.png` all draw their sheet by hand, through the shared
`SheetOverScreen` — a `Surface` with the drag handle over a scrim — because `ModalBottomSheet`
animates in and Robolectric captures the frame before it lands.

The `songs.png` shot is the one that composes two screens: `SongsContent` under
`MiniPlayerContent`, which is how `app` lays them out. The mini player lives outside the
`NavDisplay`, so no single `*Content` composable carries it.

`widget_shortcuts.png`, `widget_now_playing.png`, `lock_screen.png`, `lock_screen_expanded.png`
and `notification.png` are the images that are not generated: the home screen widgets are drawn by
the launcher, and the media controls live on the lock screen and in the notification shade — none
of them is a composable a Compose test can render. They are device captures, committed once, and
`updateReadmeScreenshots` leaves them alone — the files are listed in `manualScreenshots` in the
root `build.gradle.kts`.

To recapture one, play something on a device with a few songs in its history, take the screenshot
with `adb exec-out screencap -p > shot.png`, and scale it down in halving steps like the generated
ones. The two lock screen images are the whole screen at 520px wide, and `notification.png` is the
media card cropped out of the quick panel at the same width — cropped so no other notification and
no network name ends up in the README. Each widget is cropped out of the same home screen capture
at 600px wide. A new manual image also needs its name added to `manualScreenshots`, or the next
regeneration deletes it.

## Where to edit

The generators live in `tools/screenshots/src/test/kotlin/.../screenshots/`:

```
ReadmeScreenshotStyle.kt        # constants and the canvas background shared by both base classes below
ReadmeScreenshotsTest.kt        # base class: phone form factor, `capture`
ReadmeTabletScreenshotsTest.kt  # base class: landscape tablet, `captureTwoPane`
<Screen>Screenshots.kt          # one class per screen: the UiState it renders and the file name
```

What they render comes from `:tools:screenshot_fixtures`, shared with the screenshot tests in
[`:tools:screenshot_tests`](testing/screenshot-tests.md):

```
ScreenshotFixtures.kt      # the songs and the album every shot renders
ScreenshotArtwork.kt       # the fake Coil loader that serves the committed covers
ScreenshotQueue.kt         # queue entries, by context or queued by hand
ScreenshotPaging.kt        # a LazyPagingItems over a fixed list, for the search results
SheetOverScreen.kt         # the scrim, sheet colour and drag handle drawn around a sheet's content
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
three album covers under `tools/screenshot_fixtures/src/main/resources/artwork/` are handed to Coil by a
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

**The splash is assembled here too.** It is the system's splash window and a `View` exit animation
(`MainActivity.dissolveSplashIntoContent`), so there is no `*Content` to render. `SplashScreenshots` draws the
two drawables that animation and the window's theme use — `splash_gradient` and `splash_note`, both
in `ui:theme` — so the image still moves when the design does.

**The options sheet is assembled here.** The real sheet is a `ModalBottomSheet` opened by
`BottomSheetSceneStrategy`, which renders in a separate window that a Robolectric capture does not
see. `SongOptionsScreenshots` stacks the same parts by hand — scrim, drag handle, sheet colour —
around the real `SongOptionsContent`.

**Images are scaled in halving steps.** `updateReadmeScreenshots` halves the 1242px render until
it is close to the 520px target instead of drawing straight down to it; a single draw skips too
many pixels and leaves the app's hairline dividers and small text ragged.

**The two-pane shots build the frame by hand.** `FormFactor.Tablet10`'s built-in frame always
measures its content portrait — `TabletFrame` reads `FormFactor.logicalSize`, which comes from the
form factor's own fixed qualifiers, not from a `ScreenshotCanvas` override — so going through
`capture()` can never trigger the app's list-detail layout, which only turns on past 800dp of
*width*. `ReadmeTabletScreenshotsTest` instead renders on a landscape `ScreenshotCanvas.dp(1280,
800)` and draws its own banner and device bezel through `DeviceMockup(orientation =
MockupOrientation.Landscape)`, which swaps the frame's native width/height so the content inside it
is actually measured at 1280dp wide. `two_pane_songs.png` and `two_pane_library.png` then compose
two real `*Content` screens side by side through the app's own `ListDetailScaffold`, the same
component `ListDetailScene` renders behind the real two-pane navigation.

**The two-pane shots get their own target width.** They render two full screens beside each other
on a canvas that is wide but, at 800dp, no taller than a phone canvas is wide — scaling them down
to the same 520px as the phone shots would leave each pane with far fewer pixels per dp than a
phone screenshot gets, and both text and icons would read soft. `updateReadmeScreenshots` scales
`two_pane_songs.png` and `two_pane_library.png` to 1600px instead, in `wideScreenshots` in the root
`build.gradle.kts` — roughly the phone shots' own px-per-dp (520px / 414dp) applied to the tablet
canvas's 1280dp width.
