# Testing guide

Conventions for automated tests in this project. Follow these when adding or changing tests.

- [Test structure: Given / When / Then](given-when-then.md)
- [The `prepareScenario` factory](prepare-scenario.md)
- [Fakes & coroutine testing](fakes-and-coroutines.md)
- [Screenshot tests](screenshot-tests.md)

## At a glance

- Unit tests live in each module's `src/test/kotlin` and run on the JVM: `./gradlew testDebugUnitTest` for Android modules, `./gradlew :core:model:test` (and the other pure JVM modules) for the rest. CI runs `./gradlew testDebugUnitTest test` together with the coverage rules: 80% of the lines, for the merged report and for each new file — see [docs/code-quality.md](../code-quality.md#test-coverage).
- Compose screen tests and end-to-end flows live in `src/androidTest/kotlin` and run on a device or emulator (API 35+): `./gradlew connectedDebugAndroidTest`. Screen tests render one `*Content` composable with `createComposeExtension()` and assert on text and content descriptions; the end-to-end flow in `app` launches `MainActivity` with `createAndroidComposeExtension` and swaps the remote data source through `loadKoinModules`. Three setup details live in `build-logic`: Espresso is pinned to 3.7 because the version Compose pulls transitively crashes on Android 16; `junitPlatform.instrumentationTests.useConfigurationParameters` is off because AGP cannot pass the empty argument android-junit5 emits by default (the symptom is a run that reports zero tests); and `:app` runs its flows through the Android Test Orchestrator with `clearPackageData`, so each one starts from a fresh process and empty storage. The flows drive the whole app — playback keeps running, Koin's singletons live in the process and the library is on disk — so without it a test that leaves a song playing or a screen open decides what the next one sees.
- Stack: JUnit 6 Jupiter everywhere (`org.junit.jupiter.api`: `@Test`, `@BeforeEach`, `@AfterEach`, `@Nested`, `@ParameterizedTest`), Google Truth assertions (`assertThat(x).isEqualTo(y)`), `kotlinx-coroutines-test`, Turbine for flows, and MockK. On device, the android-junit5 plugin provides the Jupiter runner and `createComposeExtension()` for composables. No module under `app`, `core`, `feature` or `ui` has JUnit 4, the Vintage engine or Robolectric: they live only in `:tools:screenshots`, whose generators render the README's images through a Robolectric base class (see [docs/screenshots.md](../screenshots.md)), and in `:tools:screenshot_tests`, which compares every screen against a committed image (see [Screenshot tests](screenshot-tests.md)).
- Every test body is split into **Given / When / Then** sections.
- Each test class ends with a single `prepareScenario(...)` factory that assembles the system under test and its collaborators, so tests don't repeat instantiation. A `prepareScenario` that takes no parameters becomes `@BeforeEach fun setUp()` instead, and the tests drop their `// Given` block.

## Fakes and mocks

MockK is available, but a hand-written fake is the default. When a collaborator is a `fun interface`, a
lambda is the whole fake (`ObserveRecentlyPlayed { flowOf(songs) }`) and reads better in the **Given**
section than `every { ... } returns ...`. Prefer a `FakeX` class or a lambda when the test only needs
canned values or a record of the calls. Reach for MockK when the collaborator is a wide interface or a
final class you don't own (a Room DAO, an ExoPlayer) and faking it by hand would mean stubbing a dozen
members with `error("unused")`, or when the assertion is about the interaction itself (`verify { ... }`)
and a recording fake would just re-implement that.

## What is covered where

| Layer | How | Where |
|---|---|---|
| ViewModels, repositories, paging source, mappers, navigation | JUnit 6 + Truth + MockK, fakes as lambdas for `fun interface`s | `src/test` |
| Playback | The queue controller against a fake ExoPlayer timeline, the ordering rules, the session keeper and the history recorder | `core/playback/impl/src/test` |
| Database | The session round trip against a fake DAO, and against a real database: the migrations, the offline search and what the song cache is allowed to drop | `core/database/impl/src/{test,androidTest}` |
| Screens | Compose UI tests on device through the android-junit5 extension | `feature/*/src/androidTest` |
| Screenshots | Every screen compared against a committed image, under Robolectric | `tools/screenshot_tests` |
| Caches | A preview written to the media cache and read back with the network gone, and the artwork cache the app installs | `core/playback/impl/src/androidTest`, `app/src/androidTest` |
| End to end | Launches the real app, replaces the remote data source through Koin: search → player → options → album, search → play → queue a song → the queue screen, an album opened beside the songs on a wide window, which Back closes from either tab, and the player beside the songs in place of the mini player, which an album covers until Back. They pass in portrait and landscape. | `app/src/androidTest` |
