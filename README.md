# TuneScout

Search the iTunes catalog, play 30-second previews, and pick up where you left off. TuneScout is a
native Android app written for the Music AI Android code challenge.

| Splash | Recently played | Search |
| :--: | :--: | :--: |
| <img src="docs/screenshots/splash.png" width="260" alt="The splash screen, a note over the app's gradient"> | <img src="docs/screenshots/songs.png" width="260" alt="Recently played songs on the home screen"> | <img src="docs/screenshots/search.png" width="260" alt="Search results for daft punk, paged as you scroll"> |

| Player | Song options | Album |
| :--: | :--: | :--: |
| <img src="docs/screenshots/player.png" width="260" alt="The player, with artwork, timeline and transport controls"> | <img src="docs/screenshots/options.png" width="260" alt="The song options sheet, with play next, add to queue and view album"> | <img src="docs/screenshots/album.png" width="260" alt="An album and its tracks, with the queue actions in the top bar"> |

| Queue | Theme | Library |
| :--: | :--: | :--: |
| <img src="docs/screenshots/queue.png" width="260" alt="The queue sheet over the player, with songs added by hand playing before the rest of the album"> | <img src="docs/screenshots/theme.png" width="260" alt="The theme sheet over the songs screen, with light, dark and system previews and a dynamic colors toggle"> | <img src="docs/screenshots/library.png" width="260" alt="The library tab listing liked songs and playlists as a list"> |

| Library as a grid |
| :--: |
| <img src="docs/screenshots/library_grid.png" width="260" alt="The same library drawn as a grid of covers"> |

| Media controls |
| :--: |
| <img src="docs/screenshots/notification.png" width="360" alt="Media controls in the notification shade and on the lock screen"> |

The ten screens above are generated from the app's own composables, under Robolectric, by
`./scripts/screenshots.sh`; the notification shade is a device capture, since it is not a
composable. See [docs/screenshots.md](docs/screenshots.md).

## What it does

- **Search** the iTunes Search API as you type, with debounce and paginated results.
- **Play** a preview. A song tapped in search or in recently played plays on its own; a track
  tapped inside an album plays the album from there.
- **Queue** songs and whole albums by hand, either right after the current song ("Play next") or
  at the end of what you queued ("Add to queue"). What you add plays before the rest of the album
  and survives starting something else, the way Spotify's queue does. The queue screen reorders by
  drag, removes by tap, and jumps to any song. It opens as a sheet from the player or the mini
  player.
- **Pick up where you left off**: closing the app keeps the queue, the song and its position, and
  reopening restores all three, paused, from the local database.
- **Two tabs**: Home, which is search and recently played, and Your Library. The bar at the bottom
  becomes a navigation rail as soon as the window has width to spare, so a phone turned sideways
  gives the list its height back.
- **Library** of your own: liked songs and the playlists you create, as a list or a grid, with the
  choice remembered on the device. Search inside it from its own screen, which keeps the items you
  opened under "Recent searches" and lets you drop them one by one.
- **Like a song** or **add it to a playlist** from the same options sheet every list opens. Adding
  to a playlist can create one on the spot.
- **Recently played** is the first tab. It is stored locally, so it works offline and survives
  restarts. Playing a song records it once, wherever playback was started from.
- **Player** with artwork, timeline, elapsed and remaining time, play/pause, previous, next,
  repeat and the queue. Dragging the timeline seeks on release without pausing.
- **Mini player** above every screen while something is loaded, with its own play/pause and a tap
  to reopen the player.
- **Album** screen reached from the song options sheet. Fetched once through the lookup endpoint
  and cached, so it opens offline afterwards.
- **Theme** picked from a sheet on the songs screen: light, dark, or whatever the system says.
  On Android 12+ the palette can follow the wallpaper instead, explained by a dialog behind the
  (i) next to the toggle. The choice is stored on the device with DataStore, and the splash holds
  until it is read, so the first frame is already in the chosen theme.
- **Media controls** in the notification shade and on the lock screen, backed by a media session.
- Loading, empty, error, offline and rate-limited states on every screen; pull to refresh on
  search results; English and Brazilian Portuguese; content descriptions on every control.

## Running it

Requirements: JDK 21 and a device or emulator on API 26+. No API keys: the iTunes Search API is
public.

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## Checks

| Command | What it runs |
|---|---|
| `./scripts/ktlint.sh` | Formatting and the project rules, the same as CI. `--format` autocorrects. |
| `./gradlew testDebugUnitTest test` | Unit tests on the JVM (JUnit 6). |
| `./gradlew connectedDebugAndroidTest` | Compose screen tests and the end-to-end flow on a device (API 35+). |
| `./gradlew assertModuleGraph` | Module dependency rules. |

CI runs all four on every push and pull request, plus a debug and release build.

## Architecture

MVVM with unidirectional data flow, split into Gradle modules where each feature owns its own
data, domain and presentation layers and `app` wires everything together.

```
app/                 composition root: Koin modules, MainActivity, NavDisplay, permission prompt
core/
  model/             domain models, plain Kotlin
  utils/             coroutine helpers, dispatchers, duration formatting
  network/           ITunesRemoteDataSource: the iTunes API behind an interface (Ktor)
  database/          Room: songs, albums, the history, the saved session, playlists and likes
  playback/api/      the playback role interfaces every screen depends on, plain Kotlin
  playback/impl/     ExoPlayer behind those interfaces, the media session service; only app sees it
  navigation/        routes (NavKey), the Navigator event bus, back stack controller
  datastore/         the Preferences DataStore the theme preference is written to
  testing/           fixtures and a JUnit extension for Dispatchers.Main
ui/
  theme/             the light and dark palettes, dynamic color, type scale and spacing
  component/         top bar, song row, search field, seek bar, artwork, state messages
  utils/             Compose helpers
feature/
  splash/  songs/  library/  song_options/  add_to_playlist/  player/  queue/
  mini_player/  album/  theme_selection/
                     data / domain / presentation in each
tools/
  ktlint_custom_rules/
  screenshots/       renders the README's screenshots from the app's own composables
```

**Dependency rules are enforced, not hoped for.** Features never depend on features, core never
depends on a feature, `ui` knows nothing about features or data, and only `app` sees features.
`assertModuleGraph` fails the build otherwise. Shared things live in core: models, routes, the
navigator, the playback contract and the history.

**Screens.** Every screen is a `UiState` rendered by a stateless `*Content` composable, a
`UiEvent` sealed interface handled by a single `onEvent` in the ViewModel, and a `Navigator`
injected into the ViewModel. Navigation is never a UI side effect the screen has to forward.

**Navigation 3.** Routes are `@Serializable` `NavKey`s in `core/navigation`. ViewModels push
commands into a `Navigator`; one collector in `app` applies them to the back stack through
`BackStackController`, which guards against duplicate pushes and never pops the root. The song
options sheet is a route rendered by a bottom sheet scene strategy, so Songs and Player open it
the same way.

**Data.** The network layer is an interface with a Ktor implementation kept `internal`; DTOs
are mapped once to domain models and never leave the module. Room stores normalized tables
(songs, albums, history with a foreign key) and exposes flows. Repositories live in the feature
that needs them and combine the two.

**Pagination.** The iTunes API ignores `offset` and caps `limit` at 200, so the Paging 3 source
re-requests with a growing limit and keeps only the unseen tail, deduplicating by id. It is not a
cursor, but it is an honest fit for the API, and the Paging load states drive the list UI.

**Playback.** One ExoPlayer instance is shared by the app and by a `MediaSessionService` that
posts the media notification. `ObservablePlayback` publishes a `PlaybackState` every screen reads,
and a small recorder turns "first time a song plays" into a row in the history table.

**The queue has two tiers.** `PlaybackState` carries `QueueEntry` items tagged `Context` (the album
playing) or `UserQueue` (added by hand), and the play order is the context up to the current song,
then everything queued by hand, then the rest of the context. Starting another album keeps what you
queued. Adding, removing and reordering mutate the ExoPlayer timeline in place, so touching the
queue never interrupts the song that is playing. A keeper writes the queue, the current entry and
the position to Room — on every change and at most every five seconds while playing — and restores
them, paused and prepared, when the app starts.

The reasoning behind these and other choices, with what each one costs, is in
[docs/decisions.md](docs/decisions.md). Conventions for contributors and AI assistants live under
[docs/](docs/ai_agents.md).

## Testing

| Layer | How | Where |
|---|---|---|
| ViewModels, repositories, paging source, mappers, navigation | JUnit 6 + Truth + MockK, fakes as lambdas for `fun interface`s | `src/test` |
| Playback | The queue controller against a fake ExoPlayer timeline, the ordering rules, the session keeper and the history recorder | `core/playback/impl/src/test` |
| Database | The session round trip against a fake DAO, and the 1 → 2 migration against a real version 1 database | `core/database/src/{test,androidTest}` |
| Screens | Compose UI tests on device through the android-junit5 extension | `feature/*/src/androidTest` |
| End to end | Launches the real app, replaces the remote data source through Koin: search → player → options → album, and search → play → queue a song → the queue screen. Both pass in portrait and landscape. | `app/src/androidTest` |

Tests follow Given / When / Then with a `prepareScenario` factory; see
[docs/testing](docs/testing/README.md).

## Stack

Kotlin · Jetpack Compose · Navigation 3 · Koin · Ktor + kotlinx.serialization · Room 3 ·
DataStore · Paging 3 · Coil 3 · Media3 ExoPlayer · ktlint with project rules · JUnit 6 · Truth ·
MockK · Turbine

## Not done, on purpose or for lack of time

- The design uses the Articulat CF typeface, which is commercial; the app ships the system
  sans-serif with the same sizes and weights.
- The splash window Android draws before the app's first frame only accepts a flat colour, so it
  shows the same note over the gradient's average colour; the gradient itself starts with the
  first frame the app draws.
- The light palette is derived from the Figma dark one rather than designed: the file only
  specifies dark.
- The theme preference stays on the device. There is no account, so there is nothing to sync it
  to.
- The 200-item cap of the API is the end of every search; there is no "load more" beyond it.
- A playlist holds a song once: adding it again leaves it where it already is.
- Playing a song from a playlist plays that song alone. The queue only takes an album as its
  context, so a playlist is not one yet.
- Playlists and liked songs stay on the device. There is no account, so there is nothing to sync
  them to.
