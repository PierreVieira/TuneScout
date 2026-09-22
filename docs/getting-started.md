# Getting started

## Requirements

- JDK 21
- A device or emulator on API 26+ (API 35+ for the instrumented tests)

No API keys and no local secrets: the iTunes Search API is public, and `local.properties` only
needs `sdk.dir`, which Android Studio writes on the first sync.

## Running it

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration. To try it without building,
every [release](https://github.com/PierreVieira/TuneScout/releases) carries the signed APK.

## Checks

| Command | What it runs |
|---|---|
| `./scripts/ktlint.sh` | Formatting and the project rules, the same as CI. `--format` autocorrects. |
| `./gradlew testDebugUnitTest test` | Unit tests on the JVM (JUnit 6). |
| `./gradlew connectedDebugAndroidTest` | Compose screen tests and the end-to-end flow on a device (API 35+). |
| `./gradlew assertModuleGraph` | Module dependency rules. |
| `python3 scripts/check_translations.py` | Every string has its Brazilian Portuguese, Spanish, French, German and Italian translations. |

CI runs these on pull requests and pushes to `main`, each only when a file it checks changed. It
also builds a debug and a release APK and compares every screen with its committed screenshot;
every workflow is described in [Continuous integration](ci.md). What each check
enforces is in [Code quality](code-quality.md); how the suites are organised is in
[Testing](testing/README.md).

## Cutting a release

```bash
gh workflow run release -f bump=patch
```

`bump` is `patch`, `minor`, `major` or `none`. The workflow builds and signs the APK, publishes it
on a GitHub release named after the version, `1.0.0`, and, when the version changed, opens the pull
request that carries the bump to `main`. What it needs and what each step does are in [Continuous
integration](ci.md#release).

## Measuring performance

The Baseline Profile and the macrobenchmarks run by hand on a device; how, and the numbers they
gave, are in [Performance](performance.md).

## Regenerating the README screenshots

```bash
./scripts/screenshots.sh
```

Renders every screen under Robolectric, no device needed. See [README screenshots](screenshots.md).
