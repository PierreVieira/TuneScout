## Module Structure

The project uses a multi-module Gradle setup. Every new feature gets its own module.

```
app/                     # Android application: composition root (Koin appModules, TuneScoutNavigationContent, the tab host, MainActivity)
baselineprofile/         # Generates :app's Baseline Profile and benchmarks it (com.android.test, run on a device by hand — see docs/performance.md)
core/
├── model/               # Domain models shared across features (Song, Album, Playlist) — pure JVM
├── utils/               # suspendRunCatching, DispatcherProvider, IdGenerator — pure JVM
├── network/
│   ├── api/             # Remote data source interfaces (SongSearchRemoteDataSource, ...), NetworkMonitor — pure JVM
│   └── impl/            # Ktor client, DTOs, mappers, connectivity monitor, Koin module — only :app sees it
├── database/
│   ├── api/             # Local data source interfaces (SongLocalDataSource, PlaylistLocalDataSource, ...) — pure JVM
│   └── impl/            # Room database, DAOs, entities, migrations, Koin module — only :app sees it
├── datastore/           # The Preferences DataStore and its Koin module
├── navigation/          # Navigator, ChannelNavigator, NavigationCommand, BackStackController, routes
├── playback/
│   ├── api/             # Playback role interfaces (ObservablePlayback, PlaybackStarter, ...) — pure JVM
│   └── impl/            # ExoPlayer implementation, media session service, Koin module — only :app sees it
└── testing/             # Test helpers shared by feature tests (test-only dependency)
ui/
├── theme/               # TuneScoutTheme, the light and dark palettes, dynamic color, typography
├── component/           # Shared composables (Artwork, song rows, buttons)
└── utils/               # Presentation helpers (ActionViewModel and ActionCollector, the shared-element scopes and modifiers)
feature/
├── splash/
├── songs/
├── library/           # The library tab, its search, a playlist and the create dialog
├── song_options/
├── add_to_playlist/
├── player/
├── queue/
├── mini_player/
├── album/
├── theme_selection/
└── widget/            # The home screen widgets (Glance), not a route
tools/
├── ktlint_custom_rules/ # The tunescout-style ktlint ruleset
└── screenshots/         # Renders the README's screenshots (test-only, see docs/screenshots.md)
build-logic/             # Convention plugins (tunescout.android.feature, tunescout.jvm.library, ...)
```

Android modules use the `src/main/kotlin`, `src/test/kotlin` and `src/androidTest/kotlin` source sets.
Pure JVM modules (`core/model`, `core/utils`) use `src/main/kotlin` and `src/test/kotlin`.

### Within every `feature/` module

```
src/main/kotlin/com/pierre/tunescout/feature/<name>/
├── data/
│   ├── datasource/      # Local or remote data access
│   ├── dto/             # Raw data transfer objects (JSON parsing)
│   ├── mapper/          # DTO/Entity → Domain model mappers
│   └── repository/      # Repository implementations
├── di/                  # Koin module definition (one file per module)
├── domain/
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Use case interfaces + impl/ subpackage
└── presentation/
    ├── viewmodel/       # ViewModel
    ├── model/           # UiState, UiEvent, UiAction
    ├── mapper/          # Domain model → presentation model (if needed)
    ├── content/         # Content composables (Loaded/Loading variants)
    └── component/       # Sub-composables for the screen
```

`core/` modules follow the same layering where it applies (a `core/` module that only holds models or
interfaces has no `presentation/`).

## Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| Use case interface | `VerbNoun` or `VerbNounUseCase` | `ObserveRecentlyPlayed` |
| Use case impl | Same name + suffix in `impl/` | `ObserveRecentlyPlayedUseCase` |
| Repository interface | `NounRepository` | `SongsRepository` |
| Repository impl | `NounRepositoryImpl` | `SongsRepositoryImpl` |
| ViewModel | `FeatureViewModel` | `PlayerViewModel` |
| UiState | `FeatureUiState` | `PlayerUiState` |
| UiEvent | `FeatureUiEvent` | `PlayerUiEvent` |
| UiAction | `FeatureUiAction` | `PlayerUiAction` |
| Koin module | `featureModule` (val) | `playerModule` |
| Nav route | `FeatureRoute` | `PlayerRoute` |
| DTO | `NounDto` | `SongDto` |
| Entity | `NounEntity` | `SongEntity` |
| Mapper | `NounMapper` / `NounMapperImpl` | `SongMapper` |
| Root composable extension | `EntryProviderScope<NavKey>.featureEntry()` | `EntryProviderScope<NavKey>.songOptionsEntry()` |
| Gradle module | `snake_case` directory | `feature/song_options` |
| Kotlin package | the module name with the separators dropped | `com.pierre.tunescout.feature.songoptions` |

### Module names

A Gradle module's directory is `snake_case`: `feature/song_options`, `feature/mini_player`,
`tools/ktlint_custom_rules`. Single-word modules (`feature/songs`, `core/model`) need no
separator and get none.

Three names follow from that one, and they are not all spelled the same way:

| Where | Spelling | Why |
|---|---|---|
| `settings.gradle.kts`, project paths | `:feature:song_options` | the directory name |
| Typesafe accessor in a `build.gradle.kts` | `projects.feature.songOptions` | Gradle camel-cases the separator; it is not a name you choose |
| Kotlin package and `namespace` | `...feature.songoptions` | the [Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html#naming-rules) rules out underscores in package names |

So the directory and the package deliberately disagree, and renaming a module never touches a `.kt`
file.

One thing to watch in `tools/`: a JVM module's jar is named after the project, so
`tools/ktlint_custom_rules` produces `ktlint_custom_rules.jar`. Renaming it means updating
`RULESET_JAR` in [`scripts/ktlint.sh`](../../scripts/ktlint.sh) and the cache paths in
[`.github/actions/ktlint/action.yml`](../../.github/actions/ktlint/action.yml), which refer to the
jar by name.

## Dependency rules

- Features never depend on features.
- Core never depends on features.
- `:ui:*` is presentation only: it never depends on features or on core.
- Core never depends on `:ui:*`, except `:core:navigation`, whose command collector is a composable.
- Only `:app` depends on features; nothing depends on `:app`.
- Only `:app` depends on a `:core:*:impl` module (`:core:playback:impl`, `:core:database:impl`,
  `:core:network:impl`);
  features and the other core modules see the matching `:core:*:api`.

Shared things live in core: domain models (`core/model`), `NavKey` routes and the `Navigator`
(`core/navigation`), the playback interfaces (`core/playback/api`), the local data sources
(`core/database/api`) and the remote ones (`core/network/api`). Features talk to each other only through those.

### When a core module earns an `api`/`impl` split

A `:core:*` module stays a single module by default — `internal` already keeps its implementation
off every consumer's classpath, and Kotlin's ABI-based compile avoidance already means editing an
`internal` class recompiles no dependent. A split is worth its own Gradle module only when the
implementation carries something `internal` cannot hold back:

- **A manifest contribution.** `core/playback/impl` declares the exported `PlaybackService` and
  three foreground-service permissions; without the split they would merge into the manifest of
  every feature that plays a song.
- **A build step the consumers should not wait for.** `core/database/impl` runs Room's KSP
  processor, which is the heaviest task in the build; the six features that use the local data
  sources now compile against a pure-JVM module of interfaces instead of queueing behind it.
- **An implementation that is genuinely chosen, not just hidden** — ExoPlayer and Room are both
  swappable behind their interfaces, and only `:app` decides which one is wired.
- **A boundary the app is required to prove.** The challenge asks for a network layer whose API
  implementation can be replaced without affecting the other layers. `internal` makes that true;
  the split makes it enforced. `songs` and `album` compile against `core/network/api`, a pure-JVM
  module with no Ktor on its classpath, and `assertModuleGraph` fails the build the moment a
  feature depends on `core/network/impl`. The instrumented tests already rely on it: they replace
  both remote data sources through Koin and no feature notices.

Modules that fail all four keep a single module: `core/datastore`, `core/utils` and `core/model`
have no implementation worth hiding.

`feature/song_options` owns the song bottom sheet, which `songs` and `player` both open — an entry
gets its own module once something outside the module that hosts it navigates to it. They reach it
through `SongOptionsRoute` in `core/navigation`, so neither knows who draws it.

`feature/theme_selection` owns the theme sheet and the preference behind it. `app` reads that
preference through the feature's `ObserveTheme` use case, the same way it reaches any other
feature: `app` is the composition root and already depends on every one of them. The `Theme` enum
itself lives in `:ui:theme`, next to the palettes it selects, so the feature and `app` agree on it
without either owning it.

`feature/album` owns its own options sheet for the same reason: only the album screen opens it.
`feature/library` owns four routes — the tab, its search, a playlist or the liked songs, and the
create-playlist dialog — because nothing outside it opens any of them. `feature/add_to_playlist` is
a module of its own for the opposite reason: `feature/song_options` navigates to it.

The tab host itself lives in `app`, not in a `feature/home`: it composes `songs` and `library`, and
a feature may never depend on a feature. It is the same reason `MiniPlayerScaffold` is composed
there. `app` is also where the navigation bar and rail are placed, so the mini player can sit
between the content and the bar.

`feature/widget` is the other feature that is not a route: nothing in the app composes it at all.
It contributes two `GlanceAppWidgetReceiver`s to the manifest and the AppWidget host draws them,
which is why its Glance widgets, receivers and action callbacks sit in `presentation/widget` — the
one package the coverage filters leave out, since no JVM test can reach `RemoteViews` plumbing (see
[code-quality.md](../code-quality.md#what-is-measured)). What they delegate to is measured like any
other use case. `app` depends on it only to put `widgetModule` in the graph.

`feature/mini_player` is the one feature that is not a route. It exposes `MiniPlayerScaffold`, which
`app` wraps around the `NavDisplay`: the bar is laid out below every screen and owns the bottom
window insets while it is on screen. Songs and Album do not know it exists, which keeps the
feature-to-feature rule intact.

The rules are enforced by [modules-graph-assert](https://github.com/jraska/modules-graph-assert) in the
root `build.gradle.kts`:

```kotlin
restricted = arrayOf(
    ":feature:.* -X> :feature:.*",
    ":core:.* -X> :feature:.*",
    ":ui:.* -X> :feature:.*",
    ":ui:.* -X> :core:.*",
    ":core:(?!navigation).* -X> :ui:.*",
    ":feature:.* -X> :core:.*:impl",
    ":core:.* -X> :core:.*:impl",
    ":tools:.* -X> :core:.*:impl",
    ".* -X> :app",
)
```

Only the `api` and `implementation` configurations count, so a feature's tests may depend on
`:core:testing`. A new dependency that breaks a rule means the code is in the wrong module, not that the
rule needs an exception: move the shared piece down to a `:core:*` or `:ui:*` module. See
[code-quality.md](../code-quality.md) for how to run the check.
