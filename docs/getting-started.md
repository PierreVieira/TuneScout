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

Or open the project in Android Studio and run the `app` configuration.

## Checks

| Command | What it runs |
|---|---|
| `./scripts/ktlint.sh` | Formatting and the project rules, the same as CI. `--format` autocorrects. |
| `./gradlew testDebugUnitTest test` | Unit tests on the JVM (JUnit 6). |
| `./gradlew connectedDebugAndroidTest` | Compose screen tests and the end-to-end flow on a device (API 35+). |
| `./gradlew assertModuleGraph` | Module dependency rules. |
| `python3 scripts/check_translations.py` | Every string has its Brazilian Portuguese translation. |

CI runs these on pull requests and pushes to `main`, each only when a file it checks changed. It
also builds a debug and a release APK and compares every screen with its committed screenshot;
every workflow is described in [Continuous integration](ci.md). What each check
enforces is in [Code quality](code-quality.md); how the suites are organised is in
[Testing](testing/README.md).

## Regenerating the README screenshots

```bash
./scripts/screenshots.sh
```

Renders every screen under Robolectric, no device needed. See [README screenshots](screenshots.md).
