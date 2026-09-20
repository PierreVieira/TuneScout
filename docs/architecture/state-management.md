## State Management (UDF)

Every feature has three types in `presentation/model/`: `UiState`, `UiEvent`, and `UiAction`.

### UiState — what the screen shows

`UiState` does not always need to be a `sealed interface`. Use the simplest type that fits the need:

- **`sealed interface`** — when the screen has distinct states (e.g. Loading/Loaded/Error)
- **`data class`** — when the state is always "loaded" and only the data varies
- **primitive type** (`Boolean`, `String`, etc.) — when the state is simple enough

```kotlin
// When there are distinct states (Loading/Loaded)
sealed interface PlayerUiState {
    data object Loading : PlayerUiState
    data class Loaded(
        val field1: Type,
        val field2: Type,
    ) : PlayerUiState
}

// When the state is always present
data class PlayerUiState(
    val field1: Type,
    val field2: Type,
)

// When the state is a simple value
typealias PlayerUiState = Boolean
```

**UiState fields never have default values.** Every property must be explicitly set at construction. This forces the ViewModel to declare its initial state in full and prevents a new field from silently defaulting (and hiding a missing initial-value decision) when added later.

```kotlin
// Wrong — defaults hide what the initial state actually is
data class SongsUiState(
    val songs: List<SongUiModel>,
    val isSearching: Boolean = false,
)

// Correct — caller must pass every field
data class SongsUiState(
    val songs: List<SongUiModel>,
    val isSearching: Boolean,
)
```

**A UiState cannot describe a state that does not exist.** When two fields can contradict each other,
they are one field of a richer type. Two booleans are the usual smell: `isPlaying` and `hasEnded` are
never both true, so they are one `status`, and the booleans the UI reads become derived getters.

```kotlin
// Wrong — four combinations, one of them impossible; and no song leaves three fields meaningless
data class MiniPlayerUiState(
    val song: Song?,
    val isPlaying: Boolean,
    val hasEnded: Boolean,
    val progress: Float,
)

// Correct — the absent case is its own state, and one status feeds both questions
sealed interface MiniPlayerUiState {
    data object Empty : MiniPlayerUiState

    data class Loaded(
        val song: Song,
        val status: PlaybackStatus,
        val progress: Float,
    ) : MiniPlayerUiState {
        val isPlaying: Boolean
            get() = status == PlaybackStatus.Playing

        val hasEnded: Boolean
            get() = status == PlaybackStatus.Ended
    }
}
```

The same applies to a nullable field paired with a flag that only means something when it is present:
`nowPlayingId: Long?` + `isPlaying: Boolean` is one `nowPlaying: NowPlaying?`, where `NowPlaying` holds
both. Reach for, in this order: an existing enum or model from `core/model` (`PlaybackStatus`,
`NowPlaying`), a small `data class` grouping the fields that travel together, a `sealed interface` when
the screen really has distinct states. Independent flags (`isFavorite`, `isDeletable`) stay plain
booleans. No lint rule can see this one; it is a review convention.

### UiEvent — user interactions sent to the ViewModel

```kotlin
sealed interface PlayerUiEvent {
    data class OnSeek(val positionMillis: Long) : PlayerUiEvent
    data object OnPlayPauseClicked : PlayerUiEvent
    data object OnBackClicked : PlayerUiEvent
}
```

### UiAction — one-shot side effects the UI layer performs

```kotlin
sealed interface PlayerUiAction {
    data class ShowSnackBar(@StringRes val message: Int) : PlayerUiAction
    data object ScrollToTop : PlayerUiAction
}
```

Navigation is **not** a `UiAction` — inject `Navigator` and call it from the ViewModel. See
[navigation.md](navigation.md).

### ViewModel structure

```kotlin
class PlayerViewModel(
    route: PlayerRoute,
    private val useCases: PlayerUseCases,
    private val navigator: Navigator,
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState>
        field = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)

    val uiAction: SharedFlow<PlayerUiAction>
        field = MutableSharedFlow<PlayerUiAction>()

    init {
        observeSong(route.songId)
    }

    fun onEvent(event: PlayerUiEvent) = when (event) {
        is PlayerUiEvent.OnSeek -> handleSeek(event.positionMillis)
        PlayerUiEvent.OnPlayPauseClicked -> handlePlayPause()
        PlayerUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun observeSong(songId: Long) {
        useCases.observeSong(songId)
            .onEach { song -> uiState.value = PlayerUiState.Loaded(song) }
            .launchIn(viewModelScope)
    }

    private fun emitAction(action: PlayerUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }
}
```

Route arguments reach the ViewModel as a constructor parameter (`route: PlayerRoute`), supplied by Koin
through `parametersOf(route)` at the navigation entry — never through `SavedStateHandle.toRoute()`. See
[navigation.md](navigation.md).

Expose mutable state through an [explicit backing field](https://kotlinlang.org/docs/properties.html#explicit-backing-fields)
instead of a `_uiState` backing property plus `asStateFlow()`: the public type stays read-only while the class itself
smart-casts to the mutable one. Keep the type argument on the constructor (`MutableStateFlow<PlayerUiState>(...)`) — without
it the field is inferred from the initial value (`MutableStateFlow<PlayerUiState.Loading>`) and later assignments stop
compiling. The `tunescout-style:explicit-backing-field` ktlint rule flags the old pattern. A `Channel` exposed through
`receiveAsFlow()` is not a subtype of its public type, so it keeps a private backing property.
