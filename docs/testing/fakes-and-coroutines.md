# Fakes & coroutine testing

## Fakes first, mocks when they earn it

Collaborators are faked by hand by default: a `private class FakeX : X` that records calls and/or
returns canned values, placed at the end of the test file. MockK is available for the cases listed in
[README.md](README.md); a fake stays the first choice when a lambda or a few lines cover it.

For a hand-written fake to be possible, a collaborator must be an interface. When it is a concrete/`final`
class (e.g. Media3's `ExoPlayer`), introduce a small domain abstraction over it and inject that instead —
prefer a `fun interface` so it can also be faked with a lambda:

```kotlin
fun interface ObservePlaybackPosition {
    operator fun invoke(): Flow<Long>
}
// production: ObservePlaybackPositionUseCase wraps the ExoPlayer listener
// test:       ObservePlaybackPosition { flowOf(0L) }
```

A fake only needs real behavior for the methods under test; stub the rest with `error("unused")`.

## Coroutines

- Wrap test bodies in `runTest { }`, on the JVM and on device alike (`kotlinx-coroutines-test` is on
  both classpaths). Virtual time auto-advances, so timeouts/`delay` resolve without real waiting. A
  `@BeforeEach`/`@AfterEach` that seeds or clears the database is `= runTest { }` too.
- `runBlocking` is only for a helper that hands a value back to synchronous Compose test code — a
  `waitUntil { storedIds() == expected }` predicate cannot suspend. Everything else is `runTest`.
- **ViewModels** (which use `viewModelScope` / `Dispatchers.Main`): set the main dispatcher in
  `@BeforeEach` and reset it in `@AfterEach`, and opt in to the experimental test API:

  ```kotlin
  @OptIn(ExperimentalCoroutinesApi::class)
  internal class XViewModelTest {
      private val testDispatcher = UnconfinedTestDispatcher()
      @BeforeEach fun setUp() = Dispatchers.setMain(testDispatcher)
      @AfterEach fun tearDown() = Dispatchers.resetMain()
      @Test fun x() = runTest(testDispatcher) { ... }
  }
  ```

- **Long-running collectors** (a manager that collects forever, a `SharedFlow` of UI actions): launch
  them with `backgroundScope.launch { ... }` (auto-cancelled at test end) and step with `runCurrent()`.
  Collect a `SharedFlow` into a list via `backgroundScope` *before* triggering the action — it has no
  replay buffer.
- **Flows with Turbine**: `flow.test { assertThat(awaitItem()).isEqualTo(x) }` when the assertion is
  about the sequence of emissions; it fails fast on unconsumed events and reads well in **Then**.

## Gotcha: a `@Test` that returns a value is never run

Jupiter only treats a method as a test when it returns `void`/`Unit`, and a method that returns
anything else is **silently skipped** — no failure, no warning, it simply never appears in the report.
`= runTest { ... }` is safe because `TestResult` is `Unit` on the JVM, but `= runBlocking { ... }`
returns whatever its last expression evaluates to, and plenty of assertions are not `Unit`
(`assertThat(x).containsExactly(y)` returns Truth's `Ordered`).

```kotlin
// Wrong — returns Ordered, so the test never runs
@Test
fun removingAnEntry() = runBlocking {
    assertThat(observedIds()).containsExactly(1L)
}

// Correct — block body, returns Unit
@Test
fun removingAnEntry() {
    runBlocking {
        assertThat(observedIds()).containsExactly(1L)
    }
}
```

When a test you just wrote does not show up in the run, check its return type first.

## Gotcha: exception identity

`kotlinx-coroutines` stacktrace recovery may **copy** an exception as it crosses coroutine boundaries,
so the caught instance is not the same object you threw. Assert the exception **type and message**, not
identity:

```kotlin
val thrown = useCase().exceptionOrNull()
assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
assertThat(thrown).hasMessageThat().isEqualTo("boom")
```

## Misc

- `Duration` constants are `private val` in the test class, not in a `companion object` (same code-style
  rule as production).
