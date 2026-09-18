# The `prepareScenario` factory

Each test class ends with a single `private fun prepareScenario(...)` that assembles the system under
test and its collaborators (fakes). Tests call it in their **Given** section instead of repeating the
instantiation, so wiring lives in one place.

Rules:

- **`prepareScenario` never returns anything** — its return type is always `Unit`. It assigns the system
  under test (and any collaborator a test needs to drive or assert) to `lateinit var` fields declared in
  the class body. This keeps the **When**/**Then** sections reading off named fields instead of a holder.
- Place `prepareScenario` as the **last member of the test class**. Top-level `private` fakes go after
  the class.
- Parameterize it with what varies per test (e.g. a fake's behavior, an online/offline flag) and give
  those parameters sensible defaults when most tests share a value.
- If the setup needs a coroutine scope (e.g. to launch a long-running collector), make it an extension
  on `TestScope` so it can use `backgroundScope` — see [fakes-and-coroutines.md](fakes-and-coroutines.md).

## Single system under test

```kotlin
internal class AddToRecentlyPlayedUseCaseTest {
    private val maxEntries = 20
    private lateinit var useCase: AddToRecentlyPlayedUseCase

    // ... tests call prepareScenario(...) then use `useCase` ...

    private fun prepareScenario(repository: RecentlyPlayedRepository) {
        useCase = AddToRecentlyPlayedUseCase(repository = repository, maxEntries = maxEntries)
    }
}
```

## System under test plus collaborators

Declare one `lateinit var` per thing a test needs to touch — no `Scenario` holder.

```kotlin
internal class PlayerViewModelTest {
    private lateinit var viewModel: PlayerViewModel
    private lateinit var actions: List<PlayerUiAction>

    // ... tests call prepareScenario(...) then use `viewModel` / `actions` ...

    private fun TestScope.prepareScenario(song: Song) {
        viewModel = PlayerViewModel(
            route = PlayerRoute(songId = song.id),
            observeSong = { flowOf(song) },
            navigator = FakeNavigator(),
        )
        actions = mutableListOf<PlayerUiAction>().also { collected ->
            backgroundScope.launch { viewModel.uiAction.collect { collected += it } }
        }
    }
}
```
