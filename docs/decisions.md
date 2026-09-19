# Decisions and trade-offs

A running log, newest first. Each entry states the decision, why, and what it costs.

## 2026-09-19 — Two tabs, a library, and one ruler for responsiveness

**The tab host lives in `app`, not in a `feature/home`.** It composes `songs` and `library`, and a
feature may never depend on a feature — the same rule that put `MiniPlayerScaffold` in `app`.
`HomeRoute` is one entry of the root back stack and renders a nested `NavDisplay` with one
`NavBackStack` per tab, built through `rememberDecoratedNavEntries` and the `entries =` overload,
which is what preserves each tab's scroll, query and ViewModel across a switch. Cost: a second
display to keep in step with the first.

**Only the tabs live in that nested display.** The player, an album, a playlist, the library search
and every sheet are pushed onto the root back stack and cover the bar, exactly as the album screen
already did. The alternative — depth inside a tab, so the bar stays visible on a playlist the way
Spotify does — would mean teaching `Navigator` and `BackStackController` which stack a route
belongs to. That is the trade this PR declines: the bar disappears on a playlist, and the two
navigation classes stay tab-agnostic.

**The bar is hidden with `NavigationSuiteType.None`, not by removing the scaffold.** Swapping the
composable that wraps the content would rebuild the `NavDisplay` inside it and take the back stack
with it.

**Responsiveness is one ruler now: the window size class.** `SongsContent`, `AlbumContent` and
`PlayerContent` decided landscape with `maxWidth > maxHeight` inside their own `BoxWithConstraints`,
which measures whatever box they happen to sit in — a rail on the side would have changed their
answer. They now read `TuneScoutWindowSize` (`:ui:utils`, over `currentWindowAdaptiveInfo()`), and
the `*Screen` composable resolves it and passes a plain `Boolean` down, so the `*Content`
composables stay renderable on their own by the screenshot generators and the Compose tests, which
have no real window. `PlayerContent` keeps its `BoxWithConstraints`: it still needs the real `Dp`
to clamp the artwork; only the breakpoint moved. Cost: one new dependency (`material3-adaptive`),
and a `Boolean` parameter on three `*Content` signatures.

**The rail's breakpoint is written by hand.** `NavigationSuiteScaffoldDefaults.navigationSuiteType`
returns a *bar* for a compact height, which is exactly the phone turned sideways this was meant to
give a rail. The type is therefore computed from the width and height classes directly.

**Playlists and likes are rows, the view mode is a preference.** `core/database` goes to version 3
with `playlists`, `playlist_songs` (position is an explicit column), `favorite_songs` and
`library_recent_searches`; list-or-grid is a single value, so it lives in the Preferences DataStore
next to the theme.

**A recent search stores a library item, not a typed term.** That is what the Spotify screen shows,
and it is what the user removes with the `X`. The row is keyed by a string the data layer encodes
from a `LibraryItemKey`, so the liked songs — which are not a playlist row — can be one too. Cost:
no foreign key, so deleting a playlist deletes its recent search explicitly in the repository, and
a key that no longer resolves is dropped on read.

**A playlist holds a song once.** The primary key is `(playlistId, songId)`, and `appendSong`
returns early when the song is already there rather than upserting it to a new position — adding a
song twice used to move it to the end of the playlist, which the instrumented test caught.

**Liking a song and adding it to a playlist are rows in the options sheet.** The sheet is already
what every list opens for a song, so neither action needed a new entry point. Picking the playlist
is `feature/add_to_playlist`, a module of its own, because `song_options` — outside it — navigates
there; that is the same line that created `song_options` itself. The library's own four routes stay
in `feature/library`, because nothing outside opens them.

## 2026-09-19 — Playback split into api and impl

**`core/playback` is two modules: `api` holds the role interfaces, `impl` holds ExoPlayer.** Every
screen depends on `:core:playback:api`, a plain-Kotlin module with five small interfaces
(`ObservablePlayback`, `PlaybackStarter`, `Enqueuer`, `QueueControls`, `TransportControls`) and
nothing else; only `:app` depends on `:core:playback:impl`, where the ExoPlayer controller, the
media session service and the Koin module live. Before the split the same `internal` classes hid
the implementation from features, but they still shared a module with the interfaces, so a change
to the ExoPlayer wiring invalidated every feature's compile and made every feature wait for Media3
to be on the classpath. Now a feature compiles against `api` and never sees Media3 at all. The
module-graph check enforces it: `:feature:.*`, `:core:.*` and `:tools:.*` are all forbidden from
depending on `:core:playback:impl` (`:ui:.*` already cannot reach any `:core:*`). Cost: two Gradle modules
where there was one, and a second `include` line. The other core modules are not split. `model`
and `utils` have no implementation to hide; `network` and `database` would gain the same kind of
isolation from Ktor and Room, but at this size the build already finishes in seconds, so that is a
decision to take when a measurement asks for it, not before.

## 2026-09-18 — Landscape, and a foreground service that started too early

**The media service is started when the player starts playing, not when it is asked to.** Calling
`startForegroundService` gives the service five seconds to promote itself, and Media3 can only do
that once the player it wraps is actually playing. Pressing play on a song that had already finished
started the service over a player that stayed in `STATE_ENDED`, and Android killed the app with
`ForegroundServiceDidNotStartInTimeException`. The launcher now runs from
`onIsPlayingChanged(true)`, so the service only ever starts with something to show.

**A finished song replays instead of doing nothing.** ExoPlayer ignores `play()` at the end of the
timeline, so the play button used to be inert once the preview ran out. `PlayButtonState` adds a
third state to the two the button had: `Replay` seeks to zero and plays, and the player and the mini
player both show it.

**Landscape caps the content instead of stretching it.** `Modifier.readableWidth()` holds a list near
the width a phone gives it in portrait, which is what its rows were laid out for, and the parent
centres what is left. The cap comes before the fill — the other order hands `widthIn` a minimum that
is already the parent's width, which it cannot go below, and nothing is capped at all. The Songs
header puts its title beside the search field when the window is wider than it is tall, and the album
header lays its artwork beside its titles, both of which buy back a row of the list. The bottom sheet
skips its half-open state, which in a landscape window hid the last option.

**The mini player stands down while the keyboard is up.** It sits exactly where the keyboard opens,
so it was drawing a bar across the search results, and in landscape there is barely room for a row of
them as it is. Hiding it gives that room back. The check reads `WindowInsets.isImeVisible` rather
than measuring `WindowInsets.ime`, which still reports the navigation bar's height while the keyboard
is closed — measuring it hid the bar permanently.

**The end-to-end flows close the keyboard before touching a result.** They used to type and click
straight away, which works in portrait and cannot in landscape: the keyboard leaves no height for the
list, so the row is in the semantics tree but not on screen. They now send the field's IME action,
which is the same thing the search key on the keyboard does, and both flows pass in either
orientation.

**The mini player only consumed the bottom navigation bar inset, not all four.** It sits below the
content and covers the bottom bar, so that is the only side it can consume; consuming the whole
`navigationBars` inset let a landscape side bar sit on top of the list. It is also capped and centred
inside its own navigation bar padding rather than around it, so it lines up with the list above it
instead of with the window.

## 2026-09-18 — Queue, mini player and a saved session

**"Play next" and "Add to queue" differ only by where they insert.** Both tag the entry
`UserQueue`; play-next lands immediately after the current song, add-to-queue after the last thing
already queued by hand. Calling play-next twice therefore puts the most recent one first, which is
what the label promises and what the queue screen then shows.

**The queue is a bottom sheet, and reordering needs a long press because of it.** It is a route
rendered by the same `BottomSheetSceneStrategy` the song options use, so it opens over the player the
way Spotify's does. The cost is the one that made a full screen tempting first: a plain drag on a row
is swallowed by the sheet's own drag-to-dismiss, and the row goes nowhere while the sheet closes. The
handle therefore uses `longPressDraggableHandle()` — the long press claims the pointer before the
sheet can read it as a dismiss — which is also how Spotify's own queue behaves. Dragging the sheet
itself still closes it.

**Reordering uses `sh.calvin.reorderable`, and the dependency lives in `feature:queue`.** Compose
has no reorderable `LazyColumn`, and hand-rolling one is a pile of gesture and auto-scroll code. The
library is declared by the one module that reorders, rather than contained in `ui:component` the way
`compose-shimmer` is — shimmer is used by four screens, this is used by one. If a second list ever
reorders, it moves down to `ui:component`. Rows are matched by entry id, not by index: the callback
hands back `LazyListItemInfo`, and ids survive the section headers between the two tiers.

**The mini player opens the queue too.** It is the only thing on screen while browsing, so it
carries the same queue icon the player does next to its play button.

**The drag handle is not the only way to reorder.** A handle is invisible to a screen reader, so
each queued row also carries "Move up" and "Move down" as Compose custom accessibility actions,
which move the entry onto its neighbour's position — the same call the drag makes.

**`SongRow` takes a trailing slot instead of an `onMoreClick`.** The queue row needs two controls
where the others need one, and the row layout (artwork, title, subtitle) is now shared by four
screens. The `⋮` moved into `SongRowMoreAction` so the call sites still read in one line.

**The mini player is a feature, composed by `app`, not by the screens.** `MiniPlayerScaffold` wraps
the `NavDisplay`; Songs and Album never reference it, so the feature-never-depends-on-feature rule
holds and there is one place that decides where the bar appears. It is laid out below the content
rather than over it, and it consumes the navigation bar insets while it is visible, so the screen
above it never pads for a bar it no longer touches. Cost: `app` decides on which routes the bar is
allowed, which is one `when` over routes in `MiniPlayerRoutes.kt`.

**A sheet does not change which screen the user is on.** That `when` first looks past any route
marked `OverlayRoute` — the song options and the queue — because the options sheet opened from the
player is still the player, and the bar was appearing behind it. The marker lives on the routes in
`core:navigation` rather than as a list in `app`, so a new sheet cannot forget to join it.

**A closed app reopens paused, where it was.** `playback_queue` and a single-row `playback_session`
table hold the entries, the current one, the position and repeat. `PlaybackSessionKeeper` restores
before it starts recording — reversing that order would save the empty startup state over the
session it was about to read. It saves on every change that matters (queue, current song, repeat,
play/pause) and otherwise at most every five seconds while playing, which is the most a kill can
cost. Restoring calls `prepare()` but never `play()`, so the song is buffered and ready at its old
position and no notification appears until the user presses play. Cost: a 30-second preview is
fetched at launch that the user may never resume.

**The schema is exported and the migration is written by hand.** The database went to version 2
with `exportSchema = true` (`room.schemaLocation` through KSP, no extra Gradle plugin), and the
1 → 2 migration creates the two tables with the DDL Room generated for them. Destroying the
database would have been one line, but it would also throw away the recently played history on
upgrade. Version 1 had never been exported, so its schema was regenerated by compiling the old
`@Database` declaration once: with `1.json` committed next to `2.json`, `MigrationTestHelper` can
build a real version 1 database and validate the migrated one against the current entities. Cost:
`1.json` is a reconstruction rather than a historical artifact — it matches because the three
tables it describes did not change.

**The player's `MediaItem` is built behind a `MediaItemFactory`.** Building one reaches
`Uri.parse`, which is stubbed to throw in JVM unit tests, and that alone kept the whole queue
controller untestable off-device. The factory is a `fun interface` whose test double returns a
`MediaItem` carrying only the entry id, so the controller's timeline bookkeeping — the mirror list
that has to stay in step with ExoPlayer through every insert, move and removal — is now covered by
ordinary unit tests. Cost: one indirection on the hottest path in the controller.

## 2026-09-18 — Playback queue

**The queue is explicit, and has two tiers.** `PlaybackState` no longer carries a `List<Song>` that
whatever screen started playback happened to hand over; it carries a list of `QueueEntry`, each one
tagged `Context` (the album being played) or `UserQueue` (added by hand). The play order is the
context up to the current song, then everything queued by hand, then the rest of the context — the
Spotify model, where starting a different album keeps what you queued yourself. Cost: two sources to
keep straight instead of a flat list, and every mutation has to say which tier it touches.

**Tapping a search result plays that song alone.** Search and the recently-played list used to pass
the whole list as the queue, so playback rolled into songs the user never asked for. They now play a
single song under `PlaybackContext.SingleSong`. Tapping a track inside an album still plays the
album from there, which is the one place a list is the context.

**A queue entry is identified by its own id, not by the song's.** The same song can sit in the queue
twice, so `QueueEntry.id` is a uuid and it is what the `MediaItem` carries as its media id. The
previous controller looked the current song up by matching the media id against the queue, which
returned the first copy rather than the one playing.

**The ExoPlayer timeline is mutated, never rebuilt.** Adding, removing and reordering go through
`addMediaItems`/`removeMediaItem`/`moveMediaItem` on the existing timeline, so touching the queue
never interrupts the song that is playing. The controller keeps a `List<QueueEntry>` mirror of that
timeline; the ordering logic it needs lives in `QueueTimeline.kt` as pure functions, which is what
the unit tests exercise — ExoPlayer is final and faking it would test the mock.

## 2026-09-18 — README screenshots

**The README's screenshots are generated, not captured.** `./scripts/screenshots.sh` renders the
six screens from their own `*Content` composables under Robolectric, inside a phone mockup drawn by
`store-screenshots`, under an English title and description, and rewrites `docs/screenshots/`. Hand-captured shots drifted the moment a
screen changed and carried whatever the device had on it — a clock, a carrier, someone's
notifications. Generated ones are reproducible, consistent with each other, and regenerating them
is part of the change that moved the screen. Cost: the images now depend on a build task, and the
fixtures behind them are one more thing to keep plausible.

**The generators live in `:tools:screenshots`, not in each feature.** The library's base class is
Robolectric, which is JUnit 4, and this project is JUnit 6 everywhere. A module of its own keeps
the Vintage engine and Robolectric off the feature modules' test runtimes at the cost of the
fixtures sitting one module away from the screens they render.

**Album artwork is committed, not fetched.** Robolectric has no network, so three covers live in
the generators' test resources and a `FakeImageLoaderEngine` serves them by album. The fixtures
still carry real `Artwork` URLs, so the per-surface resizing runs for real. Cost: ~190 KB of
third-party cover art in the repository.

**The notification shot stays a manual capture.** The media controls are a system view in the
notification shade, which no Compose test can render. It is committed once, cropped to the media
card so it carries nothing personal, and `updateReadmeScreenshots` never touches it.

## 2026-09-18 — Design and system bars

**The screens follow the Android Phone frames of the design file, not the iOS ones.** The file
carries both; the app had been built from the iOS frames. The top bar now puts the title next to
a back arrow instead of centring it between two circular actions, the overflow menu is the
vertical `⋮`, the player is titled "Now playing" (the iOS frames title it with the album) and
lays its transport controls out from the start edge with repeat pushed to the end, and the album
top bar carries the album title.

**Every icon comes from `material-icons-extended`.** The app used to carry nine vectors traced
from the design file; they are all Material glyphs, so they are now `Icons.Rounded.*` and the
drawable folder is gone. Back is `Icons.AutoMirrored.Rounded.ArrowBack`, so it mirrors itself in
RTL, and previous/next are two glyphs instead of one rotated 180 degrees. The artifact is frozen
at 1.7.8 and ships around two thousand icons, which is the cost: R8 strips the unused ones from
the release build, and if the library is ever dropped, `TuneScoutIcons` is the single file to
change — the same containment `ui:component` already gives `compose-shimmer`.

**The player lays itself out from the space it has.** `BoxWithConstraints` picks between the
designed stacked layout and a side-by-side one when the window is wider than it is tall, and the
artwork is clamped to what is left after the title, the timeline and the controls. The design file
has no landscape frame, and the fixed 264 dp artwork plus its 100 dp of head room did not fit in a
landscape phone, so the controls fell off the screen. Cost: two layouts to keep in step, which is
why both share `PlayerDetails`.

**Screens pad themselves with `safeDrawingPadding`.** Every screen root applies it, instead of
each component reaching for `statusBarsPadding` or `navigationBarsPadding`. With edge to edge
enforced from API 35 the previous mix left content under the gesture pill and, in landscape,
under the navigation bar on the side. Cost: lists stop at the bars instead of scrolling behind
them; the gain is that a screen can never forget an inset. The Songs screen gets keyboard
insets from the same modifier, and the activity declares `adjustResize`.

## 2026-09-18 — Splash

**The system splash screen is themed to continue into the app splash.** Since API 31 every cold
start begins with a splash window drawn by the system, and by default it shows the launcher icon
over `?android:colorBackground`. `Theme.TuneScout.Splash` (through `core-splashscreen`, so API 26
behaves the same) replaces that icon with the same note artwork the Compose splash draws, sized
and centred to land on the exact same pixels, and removes the system exit animation so the icon
never jumps or scales. Cost: the note lives in `feature:splash` and the theme in `app` references
it across modules, and the icon size is expressed as a fraction of the 288 dp icon canvas.

**The system splash uses the average colour of the splash artwork.** That window only accepts a
solid colour — a gradient is not a valid value for `windowSplashScreenBackground` — so it is set
to `#000E11`, the mean colour of the designed gradient, which is also its colour at the centre of
the screen where the note sits. The app's own splash then draws the gradient from the Figma file
unchanged and without any fade, so the design is never shown half-rendered. Cost: while the
system window is up, the teal glow at the top is missing; nothing but a platform change can fix
that.

## 2026-09-18 — Loading states

**Skeletons with shimmer instead of spinners.** Every loading state (search results, next page,
player, album) renders the shape of the content it is waiting for, and artwork shimmers until
Coil delivers the bitmap. The effect comes from `compose-shimmer`, but the dependency lives only
in `ui:component` behind `Modifier.shimmer()` and `ShimmerBox`, so a future swap touches one
file. Skeleton containers carry a "Loading" content description for screen readers.

## 2026-09-18 — Data

**Paging by growing limit.** The iTunes Search API has no offset or cursor and caps `limit` at
200. `SearchSongsPagingSource` therefore asks for `delivered + pageSize` items and keeps the tail
it has not delivered yet, dropping any id already seen. Paging 3 still gives the list its load
states, retry and prefetch. Cost: page N re-downloads pages 1..N-1. Mitigated by Ktor's HTTP
cache, which honours the API's one-day `Cache-Control`, so the first page of a repeated search is
served locally and the throttling limit of about 20 calls per minute is rarely touched.

**Offline first where it matters.** The history and every song that ever appeared on screen are
stored in Room, so Player and Album open without a network. Search results are not persisted per
query: the challenge asks for the recently played list to work offline, and caching search pages
would add a table and an eviction policy without changing the experience.

## 2026-09-18 — Playback

**One ExoPlayer, shared by the app and the media service.** `ExoPlayerPlaybackController` wraps the
process-wide ExoPlayer directly; `PlaybackService` (a `MediaSessionService`) builds its
`MediaSession` over that same instance and is started when playback begins. The textbook setup
routes every screen through a `MediaController` bound to the service, but that adds an async
connection and IPC for a player that already lives in the same process. Cost: the controller
must be used from the main thread, which ViewModels already guarantee. Benefit: the notification,
lock-screen controls and the in-app player all read one state with no synchronisation code.

**Seeking never pauses.** The seek bar previews the drag locally and calls `seekTo` on release;
ExoPlayer keeps playing from the new position, the way Spotify does.

**Permissions only when needed.** `POST_NOTIFICATIONS` is requested the first time a song
actually starts playing on Android 13+, not at launch. Media controls still work if the user
declines because Android 13+ derives them from the media session, not from the notification.

**Recently played is written in one place.** A recorder observes playback state and stores a
song the first time it plays, whatever screen started it. The history table is capped at 20 rows
inside the same transaction that inserts the new one.

## 2026-09-18 — Foundation

**Multi-module with layers inside each feature.** Every `feature/*` module owns its
`data / domain / presentation` packages; `core/*` holds what more than one feature needs; `app`
wires everything. The alternative of one Gradle module per layer was rejected because it scales by
layer instead of by screen, and a change to one screen then touches three modules. Cost: more
Gradle files. Mitigated by convention plugins in `build-logic`, so a feature's build script is a
handful of lines.

**Dependency rules are checked, not hoped for.** `modules-graph-assert` forbids feature → feature,
core → feature and anything → app. Features talk through routes in `core:navigation` and shared
state in `core:*`.

**Navigation 3 with a `Navigator` event bus.** ViewModels emit navigation commands into a channel;
the single `NavDisplay` in `app` is the only place the back stack is mutated. Navigation is never a
UI side effect the screen has to forward.

**Kotlin-first libraries.** Ktor over Retrofit, kotlinx.serialization over Moshi, Koin over Hilt.
They keep the domain and data layers free of annotation processing and make the network layer easy
to swap: the rest of the app only sees an interface.

**JUnit 6 everywhere, Truth for assertions.** Local unit tests run on JUnit Jupiter through the
JUnit Platform; Compose screen tests and end-to-end flows are instrumented tests on the same
framework through the android-junit5 plugin and its Compose extension. No JUnit 4, no Vintage
engine, no Robolectric. Cost: screen tests need an API 35+ emulator instead of running on the JVM.
Benefit: one modern framework, `suspend` test methods, `@Nested` and `@ParameterizedTest`, and
screens tested on a real Android runtime.

**ktlint with project rules instead of detekt.** Formatting and a handful of conventions
(function naming, explicit backing fields, `fun interface`, `when` bracing) are enforced by a
custom ruleset in `tools/ktlint_custom_rules`, run by the same script locally and in CI.
