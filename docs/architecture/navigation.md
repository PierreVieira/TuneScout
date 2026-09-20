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

A sheet route is drawn as a bottom sheet, or as a centred dialog when the window's height class is
compact — a landscape phone has no room to open one. `BottomSheetScene` decides that, so a new sheet
route inherits the behaviour without asking for it.

Bottom sheets are routes too: the more-options sheet is `SongOptionsRoute` and the queue is
`QueueRoute`, pushed with `navigator.navigate(...)` and dismissed with `navigateBack()`. Their entries
carry the metadata of a bottom-sheet scene strategy so `NavDisplay` renders them over the previous
entry instead of replacing it, the same mechanism as `DialogSceneStrategy.dialog()` for dialogs.

A sheet route implements `OverlayRoute` as well as being registered with that metadata. The marker is
what anything reading the back stack uses to tell "which screen is the user on" from "what is drawn on
top of it" — the mini player is hidden on the player, and must stay hidden when a sheet opens over it.
Give every new sheet route the marker.

A screen does not read `LocalNavAnimatedContentScope` itself — see [Shared elements](#shared-elements).

### 4. Register in `TuneScoutNavDisplay`

```kotlin
// app/src/main/kotlin/com/pierre/tunescout/navigation/TuneScoutNavDisplay.kt
entryProvider = entryProvider<NavKey> {
    splash()
    home(tabsState)
    player()
    album()
}
```

`app` is the only module that depends on every feature, which is why the registration lives there.

## The tab host

`HomeRoute` is one entry of the root back stack, and it renders a **second `NavDisplay`** with one
`NavBackStack` per tab (`app/navigation/home/`). The tab entries are built with
`rememberDecoratedNavEntries` and handed to the `entries =` overload — not `backStack =` — which is
what gives each tab its own saved state and its own `ViewModelStore` across a switch.

While a tab other than the first is selected, the first tab's entries stay at the head of that
list, so system back animates from the second tab to the first the way Android expects, and
`onBack` only has to move the selection.

Only the two tabs live in the nested display. **Everything a tab opens — the player, an album, a
playlist, the queue, any sheet — is pushed onto the root back stack**, over the bar, through the
same `Navigator`. That is what keeps the `Navigator` and the `BackStackController` free of any
notion of tabs.

The bar itself is drawn by `TuneScoutNavigationSuite` (`:ui:component`) *outside* the root
`NavDisplay`, so the mini player sits between the content and the bar. It is hidden with
`NavigationSuiteType.None` rather than by removing the scaffold: removing it would rebuild the
`NavDisplay` underneath and take the back stack with it.

## Shared elements

The artwork of a song follows it from wherever it is tapped into the player, and back out again on
the way home. The same flight carries the title and the artist.

### The wiring

`TuneScoutNavDisplay` wraps a `SharedTransitionLayout` around **both** the `NavDisplay` and the
`MiniPlayerScaffold` and publishes the scope as `LocalSharedTransitionScope` (`:ui:utils`). Flying
between the bar and a screen only works while the two sit in the same scope.

A shared element also needs the visibility scope of whoever draws it, and that differs by drawer:

| Drawer | Visibility scope | Provided by |
|---|---|---|
| A screen inside the `NavDisplay` | `LocalNavAnimatedContentScope.current` | `rememberSharedElementNavEntryDecorator()`, registered in `entryDecorators` |
| The mini player bar | its own `AnimatedVisibility` | `MiniPlayerScaffold` |

Both publish the pair as `LocalSharedElementScopes`, so a screen never names either scope: it tags
the element and nothing else.

```kotlin
Artwork(
    url = song.artwork.largeUrl,
    cornerPercent = ARTWORK_CORNER_PERCENT,
    sharedKey = getSongSharedKey(song.id, SongSharedElement.ARTWORK),
    modifier = Modifier.size(size),
)
```

`LocalSharedElementScopes` is `null` by default, and `Modifier.sharedArtwork`/`sharedTextBounds` are
a no-op without it. That is what keeps every `*Content` composable renderable under `androidTest`
and `:tools:screenshots`, where there is no `NavDisplay` to provide a scope.

`Artwork` takes a corner **percent**, not a `Dp`, so the radius follows the animated bounds instead
of snapping to the target's on the first frame.

### One key, one source

A key may only be flown by one element at a time, and the song that is playing is on screen twice:
its row in the list and the mini player bar. The one that flies is **the one the finger landed on**
— which, since a row only starts a song and never opens the player, is always the bar.

Each surface declares itself with `LocalSharedArtworkSurface` (`SongRow` is `LIST_ROW`,
`MiniPlayerContent` is `MINI_PLAYER`; the player declares nothing, since it is the other end of
every flight), and the one that navigates writes itself into `LocalTappedSharedArtworkSurface` as it
is tapped. Only `MiniPlayerContent` does, so a row's keys are never claimed: a surface that is not
the tapped one gets no modifier at all, and before the first tap none does. The rows keep passing
their keys so that the day something flies out of one, only the write is missing.

The pop reads the same state, so the artwork returns to whichever surface it came from.

The bar also holds the song it was drawing once it starts leaving (`rememberBarSong`): opening the
player changes what is playing a frame or two later, and a bar that swapped its song mid-fade would
read as a glitch.

### Sheets do not take part

`LocalNavAnimatedContentScope` is a no-op inside an `OverlayScene`, so the options and queue sheets
get no shared elements — and a queue row does not navigate anyway, it swaps the song in the player
already behind the sheet. Their rows pass no key.

### Timing

`NavTransitions.kt` replaces the Navigation 3 default 700 ms fade with a 350 ms one, and drops the
`scaleOut` from the predictive-back spec: a screen that scales while an element flies over it drags
the eye off the element.

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
