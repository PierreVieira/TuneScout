## Navigation

The app uses Navigation 3 (`androidx.navigation3:navigation3-ui`, Android only). The back stack is a
`NavBackStack<NavKey>` owned by `TuneScoutNavDisplay` in `app`; there is no `NavController`.

### 1. Define a type-safe route in `core/navigation/src/main/kotlin/.../route/`

Every route is a `@Serializable` `NavKey`, one per file:

```kotlin
// No arguments
@Serializable
data object SongsRoute : NavKey

// With arguments
@Serializable
data class PlayerRoute(
    val songId: Long,
) : NavKey
```

No polymorphic serializers module needs to be registered: `rememberNavBackStack(SongsRoute)` restores
state through reflection on Android.

### 2. Navigate through the `Navigator`

Navigation is a single injectable dependency, not a `NavController` and not lambdas threaded down
from the root. Inject `Navigator` (`core/navigation`) into the ViewModel and call it:

- `navigate(route: NavKey)` — push a route
- `navigateBack()` — pop the top entry
- `navigateReplacingTop(route: NavKey)` — pop the current entry and push a route (the old
  `popUpTo(current) { inclusive = true }` pattern)

```kotlin
internal class SongsViewModel(
    private val navigator: Navigator,
) : ViewModel() {
    fun onEvent(event: SongsUiEvent) = when (event) {
        is SongsUiEvent.OnSongClicked -> navigator.navigate(PlayerRoute(event.songId))
        is SongsUiEvent.OnMoreOptionsClicked -> navigator.navigate(SongOptionsRoute(event.songId))
        SongsUiEvent.OnBackClicked -> navigator.navigateBack()
    }
}
```

`Navigator` is an event bus: the `ChannelNavigator` implementation pushes a `NavigationCommand`
(`Navigate`, `ReplaceTop`, `Back`) into a buffered channel, and `TuneScoutNavDisplay` collects the
commands and applies them to the back stack. The back stack is only ever mutated in that one place,
inside the composition.

**Navigation never belongs in a `UiAction`.** `UiAction` is for effects the UI layer has to perform
itself — snackbars, clipboard, share sheets, scrolling, focus.

When a back callback is pure UI and never passes through a `UiEvent` — a bottom sheet's dismiss, for
example — resolve the `Navigator` in the entry with `koinInject<Navigator>()` and pass
`navigator::navigateBack` down. Do not reach into DI from leaf composables; they keep their lambda
parameters.

### 3. Create an `EntryProviderScope` extension in `presentation/`

```kotlin
// PlayerRoot.kt
fun EntryProviderScope<NavKey>.player() {
    entry<PlayerRoute> { route ->
        val viewModel = koinViewModel<PlayerViewModel> { parametersOf(route) }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        PlayerContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
        )
    }
}
```

Route arguments reach the ViewModel as a constructor parameter (`route: PlayerRoute`) injected via
`parametersOf(route)` — never via `SavedStateHandle.toRoute()`.

Bottom sheets are routes too: the more-options sheet is `SongOptionsRoute`, pushed with
`navigator.navigate(SongOptionsRoute(songId))` and dismissed with `navigateBack()`. Its entry carries
the metadata of a bottom-sheet scene strategy so `NavDisplay` renders it over the previous entry instead
of replacing it, the same mechanism as `DialogSceneStrategy.dialog()` for dialogs.

For shared-element transitions, the entry's animation scope is `LocalNavAnimatedContentScope.current`.

### 4. Register in `TuneScoutNavDisplay`

```kotlin
// app/src/main/kotlin/com/quare/tunescout/navigation/TuneScoutNavDisplay.kt
entryProvider = entryProvider<NavKey> {
    splash()
    songs()
    player()
    album()
}
```

`app` is the only module that depends on every feature, which is why the registration lives there.

## Composable Structure

```kotlin
// presentation/content/PlayerContent.kt
@Composable
fun PlayerContent(
    uiState: PlayerUiState,
    onEvent: (PlayerUiEvent) -> Unit,
) {
    when (uiState) {
        PlayerUiState.Loading -> PlayerLoading()
        is PlayerUiState.Loaded -> PlayerLoaded(uiState, onEvent)
    }
}

@Composable
private fun PlayerLoaded(
    uiState: PlayerUiState.Loaded,
    onEvent: (PlayerUiEvent) -> Unit,
) {
    ...
}
```

Sub-composables go in `presentation/component/`.
