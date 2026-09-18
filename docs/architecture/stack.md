## Stack

- **Language:** Kotlin 2.4, native Android only (`minSdk 26`), kotlinx.coroutines for async
- **UI:** Jetpack Compose with Material 3
- **Dependency injection:** Koin (`koin-android`, `koin-androidx-compose`)
- **Network:** Ktor client (OkHttp engine) behind the `ITunesApi` interface in `core/network`
- **Serialization:** kotlinx.serialization (JSON)
- **Local data:** Room 3 (`androidx.room3`) with the bundled SQLite driver
- **Paging:** Paging 3 (`paging-common` + `paging-compose`)
- **Navigation:** Navigation 3 (`androidx.navigation3`, Android only) with type-safe `@Serializable` `NavKey` routes
- **Images:** Coil 3 with the Ktor network fetcher
- **Playback:** Media3 ExoPlayer (`media3-exoplayer` + `media3-session`)
- **Lint:** ktlint with the custom `tunescout-style` ruleset in `tools/ktlint-custom-rules`, run through `./scripts/ktlint.sh`
- **Tests:** JUnit 6 Jupiter + Google Truth + kotlinx-coroutines-test + Turbine + MockK; Compose screen tests and E2E run on device with the android-junit5 plugin (`createComposeExtension()`), no Robolectric
