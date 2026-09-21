## Navigation

The app uses Navigation 3 (`androidx.navigation3:navigation3-ui`, Android only). The back stack is a
`NavBackStack<NavKey>` owned by `TuneScoutNavigationContent` in `app`; there is no `NavController`.

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
- `navigateToDeepLink(route: NavKey)` — replace the whole back stack with the route and its synthetic
  parents, root first (see [Deep links](#deep-links))

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
(`Navigate`, `ReplaceTop`, `ResetTo`, `Back`) into a buffered channel, and `TuneScoutNavigationContent` collects the
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

### 4. Register in `TuneScoutNavigationContent`

```kotlin
// app/src/main/kotlin/com/pierre/tunescout/navigation/TuneScoutNavigationContent.kt
entryProvider = entryProvider<NavKey> {
    splash()
    home(tabsState)
    player()
    album()
}
```

`app` is the only module that depends on every feature, which is why the registration lives there.

## List and detail on a wide window

On a window of expanded width, an album opens **beside** the tab host instead of covering it.
`ListDetailSceneStrategy` (`core/navigation/scene/`) draws the two entries as one `ListDetailScene`:
the list on the left, the detail on a raised card on the right (`ListDetailScaffold`, `:ui:component`).
The strategy is created in `TuneScoutNavigationContent` from `rememberWindowSize().isWidthExpanded`,
keyed on it, since `NavDisplay` only calculates its scenes again when its strategies or entries change.
It comes after the sheet and dialog strategies, so a sheet opened over the two panes covers both.

Two things make an entry take part, and a route that does needs both:

| | For | Where |
|---|---|---|
| `ListDetailSceneStrategy.listPane()` / `detailPane()` | the scene, which only sees entries | the entry's `metadata` |
| `DetailPaneRoute` | anything reading the back stack's keys | the route |

The marker is what keeps the rail on screen: `isHomeVisible(isTwoPane)` counts the tab host as
visible while a detail sits beside it. Both read the same rule, `findListPaneIndexOrNull`: the top
of the stack is a detail, and the first entry under the details on top is a list.

The back stack never depends on the width. `[HomeRoute, AlbumRoute]` is the same stack in portrait
and in landscape; rotating only changes the scene, and Back pops the album either way. A detail
pushed over a detail replaces it in the right pane, and Back walks through them there.

The tab host alone keeps the two panes too: the strategy lays it beside `emptyDetailPane`, which
`TuneScoutNavigationContent` fills with `NowPlayingScreen` (`:feature:player`) — the player of
whatever is playing, or an empty state until something is. It is not an entry of the back stack, so
it has no back arrow, and the mini player is hidden while it is on screen
(`isMiniPlayerAllowed(isTwoPane)`). An album opened from the tabs replaces it in the right pane, and
Back brings it back. The list alone has a scene key of its own, so opening the first detail and
closing the last one change the scene, as they did when the list alone was a single pane; only a
detail swapped for another crossfades inside the right pane. An entry leaving a scene that stays
kept Compose from ever going idle under the test clock, and Back then timed out in the flow tests.
`PlayerRoute` is a detail too: a song tapped while it is already playing opens the player in the
right pane — over an album, if one is open — rather than over the tabs.

The detail pane provides `LocalIsInDetailPane`, which a screen reads to lay itself out for the pane
rather than the window: the player stacks its artwork over the controls there, and when the window is
as short as a phone on its side it takes `PlayerLayout.Compact`, a thumbnail beside the title with the
timeline and the controls at full width under them (`PlayerLayout.of`).

A list that runs a navigation of its own wraps it in `DeferBackToDetailPaneScaffold`. The tab host does:
its nested `NavDisplay` was registered with the back dispatcher before the album was pushed, and
handlers are asked most recent first, so it would switch tabs where Back should close the album.

## Deep links

The home screen widgets open the app on a screen of its own. The pieces follow the
[Navigation 3 deep link guide](https://github.com/android/nav3-recipes/blob/main/docs/deeplink-guide.md),
adapted to this app: `androidx.navigation3.runtime.deeplink` is not in the stable release the
project uses, and the URLs are ours rather than a public `https` domain, so no URI pattern matching
or serializer decoding is needed.

`core/navigation/deeplink/` holds the four pieces:

| Piece | What it does |
|---|---|
| `DeepLinkUrls` | the URL space — the `tunescout` scheme and the builders. Whoever opens a link and whoever reads one back both go through it, so the shapes cannot drift |
| `DeepLinkKey` | a route something outside the app can open, with the `parent` it sits under |
| `TuneScoutDeepLinkMatcher` | a URL back into a route, or `null` when it names none |
| `SyntheticBackStackFactory` | the route plus its ancestors, root first |

`MainActivity` hands the intent's URL to `MainViewModel.onDeepLinkReceived` in `onCreate` and in
`onNewIntent` — it is `singleTop`, so a widget tapped while the app is open lands on the running
instance. The ViewModel matches it and calls `navigateToDeepLink` with the route; `ChannelNavigator`
builds the synthetic back stack through `SyntheticBackStackFactory` and emits one `ResetTo`. The
activity knows nothing of the matcher or the navigator, so the whole path is unit-tested there.
`onCreate` only does this on a fresh launch: a recreated activity (rotation, process death) still
carries its launch intent, but the restored back stack already holds the deep link and whatever the
user opened after it. The command is emitted *before* composition starts; the navigator's
unlimited channel holds it until the collector attaches, so the first back stack the app draws is
already the deep link's and the splash is never shown on top of a screen the user asked for.

The synthetic back stack is what the guide's first principle asks for: a deep link into
`PlayerRoute` lands on `[HomeRoute, PlayerRoute]`, so Back leads where it would have led had the
user navigated there by hand instead of dropping them out of the app. A new deep-linkable route
implements `DeepLinkKey`, names its `parent`, and gets a branch in the matcher.

The scheme is declared in `app`'s manifest without the `BROWSABLE` category: these links are for the
app's own surfaces, not for the browser.

## Asking the screen under a sheet

A sheet sometimes needs the screen it was opened over to do something as it closes — the song
options sheet asking an album or a playlist to start reordering, for one. That cannot travel as a
route: nothing is pushed, the sheet is popped. It goes through a small request channel in
`core/navigation` instead, injected like the `Navigator`:
[`ReorderRequests`](../../core/navigation/src/main/kotlin/com/pierre/tunescout/core/navigation/reorder/ReorderRequests.kt).
The sheet calls `request(target)` and then `navigateBack()`; the screen's ViewModel collects
`observe(target)` in its `viewModelScope` from `init`, so it hears the request while the sheet is
still on top of it. The channel replays nothing, and a route that needs one carries the target the
sheet should offer to act on (`SongOptionsRoute.reorderTarget`).

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
same `Navigator` — an album on a wide window included, which the root display then draws beside the
tabs (see [List and detail on a wide window](#list-and-detail-on-a-wide-window)). That is what keeps the `Navigator` and the `BackStackController` free of any
notion of tabs.

The bar itself is drawn by `TuneScoutNavigationSuiteScaffold` (`:ui:component`) *outside* the root
`NavDisplay`, so the mini player sits between the content and the bar. It is hidden with
`NavigationSuiteType.None` rather than by removing the scaffold: removing it would rebuild the
`NavDisplay` underneath and take the back stack with it.

## Shared elements

The artwork of a song follows it from wherever it is tapped into the player, and back out again on
the way home. The same flight carries the title and the artist.

### The wiring

`TuneScoutNavigationContent` wraps a `SharedTransitionLayout` around **both** the `NavDisplay` and the
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

### The bar waits for the player to start leaving

The back stack and the `NavDisplay` do not move together. The back stack changes at once, but the
`NavDisplay` only turns its transition around a few frames later, from a `LaunchedEffect`. On the way
in that gap is harmless: the player is not composed until the transition has turned. On the way home
it is not: a bar that followed the back stack alone would come back while the player still counts as
the *target* of its keys, the two would claim the same key as targets, and the flight would land on
its first frame.

So the player declares itself with `SharedArtworkDestinationEffect()` (`PlayerScreen`), which
publishes its entry's transition as `LocalSharedArtworkDestination`, and `MiniPlayerScaffold` shows
the bar only once `isStaying` is false — once the `NavDisplay` has started taking the player away. A
predictive back gesture turns the transition before the back stack pops, so the bar comes back the
moment the gesture commits.

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
