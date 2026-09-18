# TuneScout

Search the iTunes catalog, play 30-second previews, and pick up where you left off. TuneScout is a
native Android app written for the Music AI Android code challenge.

> Work in progress. The sections below grow with the project; decisions and trade-offs are
> collected in [docs/decisions.md](docs/decisions.md) as they are made.

## Running it

Requirements: JDK 21 and an Android device or emulator on API 26+. No API keys: the iTunes Search
API is public.

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## Checks

```bash
./scripts/ktlint.sh              # lint (same as CI); add --format to autocorrect
./gradlew testDebugUnitTest test # unit tests
./gradlew assertModuleGraph      # module dependency rules
```

## Stack

Kotlin · Jetpack Compose · Navigation 3 · Koin · Ktor + kotlinx.serialization · Room 3 · Paging 3 ·
Coil 3 · Media3 ExoPlayer · ktlint (with project-specific rules) · JUnit 6 + Truth + MockK + Turbine

## Layout

```
app/                 composition root: Koin modules, MainActivity, NavDisplay
core/
  model/             domain models, plain Kotlin
  utils/             coroutine helpers, dispatchers
  navigation/        routes (NavKey), the Navigator event bus and the back stack controller
  network/           iTunes Search API behind ITunesRemoteDataSource (Ktor)
ui/
  theme/             colors, typography, TuneScoutTheme
  component/         shared composables (artwork, rows, buttons)
  utils/             Compose helpers (ActionCollector)
feature/
  ...                one module per screen, each with data / domain / presentation
tools/
  ktlint-custom-rules/
```

Features never depend on each other, core never depends on a feature, `ui` knows nothing about
features or data, and only `app` sees features. These rules are enforced at build time by
`assertModuleGraph`. Conventions for
contributors (and for AI assistants) live under [docs/](docs/ai_agents.md).
