# Test structure: Given / When / Then

Every test body is split into three sections, each marked with a `// Given`, `// When`, `// Then`
comment and separated by a blank line. (These markers are intentional structure, and the only `//`
comments the `kdoc-only-comments` rule lets through: any other explanation is KDoc, on the test function
or on a helper extracted to carry it — see [Code style](../architecture/code-style.md#comments).)

- **Given** — build the scenario and inputs (usually a single `prepareScenario(...)` call; see
  [prepare-scenario.md](prepare-scenario.md)).
- **When** — exercise the single action under test. Keep it to one behavior per test.
- **Then** — assertions on the resulting state and/or recorded interactions.

## Naming

Test names are backticked, spaced sentences that **mirror the three sections**, in the form
`` `GIVEN <state> WHEN <action> THEN <outcome>` ``. The `GIVEN`/`WHEN`/`THEN` keywords are **uppercase**
in the name (so they stand out as structure, not prose) and line up with the `// Given` / `// When` /
`// Then` blocks of the body, so the name reads as a summary of the test and the body reads as its
expansion.

```
GIVEN a network failure WHEN refreshing THEN keeps the cached songs without a snackbar
└──── GIVEN ────┘ └── WHEN ──┘ └──────────────────── THEN ─────────────────────────┘
```

## Example

```kotlin
@Test
fun `GIVEN a loaded song WHEN clicking play THEN emits ShowNowPlaying`() = runTest(testDispatcher) {
    // Given
    prepareScenario(song = song())

    // When
    viewModel.onEvent(PlayerUiEvent.OnPlayPauseClicked)

    // Then
    assertThat(actions).containsExactly(PlayerUiAction.ShowNowPlaying)
}
```
