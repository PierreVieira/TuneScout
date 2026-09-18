# Testing guide

Conventions for automated tests in this project. Follow these when adding or changing tests.

- [Test structure: Given / When / Then](given-when-then.md)
- [The `prepareScenario` factory](prepare-scenario.md)
- [Fakes & coroutine testing](fakes-and-coroutines.md)

## At a glance

- Unit tests live in each module's `src/test/kotlin` and run on the JVM: `./gradlew testDebugUnitTest` for Android modules, `./gradlew :core:model:test` (and the other pure JVM modules) for the rest. CI runs `./gradlew testDebugUnitTest test`.
- Compose screen tests and end-to-end flows live in `src/androidTest/kotlin` and run on a device or emulator (API 35+): `./gradlew connectedDebugAndroidTest`. Screen tests render one `*Content` composable with `createComposeExtension()` and assert on text and content descriptions; the end-to-end flow in `app` launches `MainActivity` with `createAndroidComposeExtension` and swaps the remote data source through `loadKoinModules`. Two setup details live in `build-logic`: Espresso is pinned to 3.7 because the version Compose pulls transitively crashes on Android 16, and `junitPlatform.instrumentationTests.useConfigurationParameters` is off because AGP cannot pass the empty argument android-junit5 emits by default (the symptom is a run that reports zero tests).
- Stack: JUnit 6 Jupiter everywhere (`org.junit.jupiter.api`: `@Test`, `@BeforeEach`, `@AfterEach`, `@Nested`, `@ParameterizedTest`), Google Truth assertions (`assertThat(x).isEqualTo(y)`), `kotlinx-coroutines-test`, Turbine for flows, and MockK. On device, the android-junit5 plugin provides the Jupiter runner and `createComposeExtension()` for composables. There is no JUnit 4, no Vintage engine and no Robolectric.
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
