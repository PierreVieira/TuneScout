## Module Structure

The project uses a multi-module Gradle setup. Every new feature gets its own module.

```
app/                     # Android application: composition root (Koin appModules, TuneScoutNavDisplay, MainActivity)
core/
├── model/               # Domain models shared across features (Song, Album) — pure JVM
├── utils/               # suspendRunCatching, DispatcherProvider — pure JVM
├── network/             # ITunesRemoteDataSource interface + Ktor implementation, DTOs (internal)
├── database/            # Room database, DAOs, entities
├── navigation/          # Navigator, ChannelNavigator, NavigationCommand, BackStackController, routes
├── playback/            # Playback interface over Media3 ExoPlayer
└── testing/             # Test helpers shared by feature tests (test-only dependency)
ui/
├── theme/               # TuneScoutTheme, colors, typography
├── component/           # Shared composables (Artwork, song rows, buttons)
└── utils/               # Compose helpers (ActionCollector)
feature/
├── splash/
├── songs/
├── player/
└── album/
tools/
└── ktlint-custom-rules/ # The tunescout-style ktlint ruleset
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
| Root composable extension | `EntryProviderScope<NavKey>.featureName()` | `EntryProviderScope<NavKey>.player()` |

## Dependency rules

- Features never depend on features.
- Core never depends on features.
- `:ui:*` is presentation only: it never depends on features or on core.
- Core never depends on `:ui:*`, except `:core:navigation`, whose command collector is a composable.
- Only `:app` depends on features; nothing depends on `:app`.

Shared things live in core: domain models (`core/model`), `NavKey` routes and the `Navigator`
(`core/navigation`), the playback interface (`core/playback`) and the recently-played repository
(`core/database`). Features talk to each other only through those.

The rules are enforced by [modules-graph-assert](https://github.com/jraska/modules-graph-assert) in the
root `build.gradle.kts`:

```kotlin
restricted = arrayOf(
    ":feature:.* -X> :feature:.*",
    ":core:.* -X> :feature:.*",
    ":ui:.* -X> :feature:.*",
    ":ui:.* -X> :core:.*",
    ":core:(?!navigation).* -X> :ui:.*",
    ".* -X> :app",
)
```

Only the `api` and `implementation` configurations count, so a feature's tests may depend on
`:core:testing`. A new dependency that breaks a rule means the code is in the wrong module, not that the
rule needs an exception: move the shared piece down to a `:core:*` or `:ui:*` module. See
[code-quality.md](../code-quality.md) for how to run the check.
