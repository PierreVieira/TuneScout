## Stack

- **Language:** Kotlin 2.4, native Android only (`minSdk 26`), kotlinx.coroutines for async
- **UI:** Jetpack Compose with Material 3, plus `material3-adaptive` for the window size class and
  `material3-adaptive-navigation-suite` for the bar-or-rail the two tabs are drawn in
- **Dependency injection:** Koin (`koin-android`, `koin-androidx-compose`)
- **Network:** Ktor client (OkHttp engine) behind the `SongSearchRemoteDataSource` and `AlbumRemoteDataSource` interfaces in `core/network/api` (implemented in `core/network/impl`)
- **Serialization:** kotlinx.serialization (JSON)
- **Local data:** Room 3 (`androidx.room3`) with the bundled SQLite driver; Preferences DataStore
  (`core/datastore`) for the handful of user preferences that are not rows
- **Paging:** Paging 3 (`paging-common` + `paging-compose`)
- **Navigation:** Navigation 3 (`androidx.navigation3`, Android only) with type-safe `@Serializable` `NavKey` routes
- **Images:** Coil 3 with the Ktor network fetcher
- **Playback:** Media3 ExoPlayer (`media3-exoplayer` + `media3-session`)
- **Performance:** a committed Baseline Profile and startup profile (`androidx.baselineprofile`
  plugin, `profileinstaller` in `:app`), generated and measured by Macrobenchmark and UiAutomator in
  `:tools:baseline_profile`; a Compose stability configuration in `compose_stability.conf`. See
  [Performance](../performance.md)
- **Speech:** the platform's `android.speech.SpeechRecognizer`, behind `feature/audio_search`
- **Lint:** ktlint with the custom `tunescout-style` ruleset in `tools/ktlint_custom_rules`, run through `./scripts/ktlint.sh`
- **Tests:** JUnit 6 Jupiter + Google Truth + kotlinx-coroutines-test + Turbine + MockK; Compose screen tests and E2E run on device with the android-junit5 plugin (`createComposeExtension()`), no Robolectric
