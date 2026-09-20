# Code Style

## Comments

Documentation is written as **KDoc** (`/** ... */`) on a declaration, and nowhere else: no `//`, no
`/* */`. A name or a small extracted function carries the intent first; when a *why* still needs saying —
a platform quirk, a constraint the code cannot show — it goes in the KDoc of the declaration it explains,
where the IDE shows it at every call site.

```kotlin
// Correct — the reason lives on the declaration it explains
/**
 * The song the bar draws, held at its last value once the bar starts leaving.
 */
@Composable
internal fun rememberBarSong(song: Song?, isVisible: Boolean): Song? { ... }

// Wrong — a line comment, invisible outside this file
// Holds the last song while the bar leaves
@Composable
internal fun rememberBarSong(song: Song?, isVisible: Boolean): Song? { ... }
```

A comment in the middle of a function body has nothing to attach to. Either it describes the whole
function — move it to the function's KDoc — or it describes one step, and that step is extracted into a
function or a `val` whose KDoc carries the text:

```kotlin
// Wrong
ModalBottomSheet(
    // A landscape window is short enough that the half-open state hides the last option.
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
)

// Correct
ModalBottomSheet(sheetState = rememberFullyExpandedSheetState())

/** A landscape window is short enough that the half-open state hides the last option. */
@Composable
private fun rememberFullyExpandedSheetState(): SheetState =
    rememberModalBottomSheetState(skipPartiallyExpanded = true)
```

### KDoc tags

A KDoc follows the shape of the [official Kotlin documentation](https://kotlinlang.org/docs/kotlin-doc.html).
The summary says what the declaration is and why it is the way it is; the tags then describe its parts:

- **A documented class describes everything its constructor takes**: `@property` for each `val` / `var`,
  `@param` for each plain parameter and each type parameter, in declaration order.
- **A documented function that returns a value says what with `@return`.** Its parameters are referred to
  inline, as `[name]` links in the text, and get a `@param` only when one needs a longer explanation.
- A tag starts in lower case and ends with a period, after one blank ` *` line.

```kotlin
// Correct
/**
 * The state behind the tab host: one back stack per tab, and which one is on screen.
 *
 * @property backStacks the back stack of each tab.
 * @param selectedIndexState the saveable holder of the selected tab's position in [HomeTab.entries].
 */
internal class HomeTabsState(
    private val backStacks: Map<HomeTab, NavBackStack<NavKey>>,
    selectedIndexState: MutableIntState,
)

/**
 * The song the bar draws, held at its last value once the bar starts leaving.
 *
 * @return [song] while [isVisible], and the last song seen once it is not.
 */
@Composable
internal fun rememberBarSong(song: Song?, isVisible: Boolean): Song?

// Wrong — the reader still has to open the class to learn what it holds, and the function to learn
// what comes back
/** The state behind the tab host. */
internal class HomeTabsState(private val backStacks: ..., selectedIndexState: MutableIntState)

/** The song the bar draws. */
@Composable
internal fun rememberBarSong(song: Song?, isVisible: Boolean): Song?
```

Nothing here asks for a KDoc to exist — a declaration whose name says it all still has none. The tags
complete the KDocs that do exist.

This is enforced by the custom ktlint rule `tunescout-style:kdoc-tags`, which also reports a `@param` /
`@property` naming something the constructor no longer declares, so a rename cannot leave a stale tag
behind.

The `// Correct` / `// Wrong` markers in this document are annotations for the examples, not something to
copy into code. Test bodies keep one exception: their `// Given` / `// When` / `// Then` section markers
are intentional structure — see [docs/testing/given-when-then.md](../testing/given-when-then.md). Any
other explanation in a test follows the same rule as production code.

This is enforced by the custom ktlint rule `tunescout-style:kdoc-only-comments`, which reports every
`//` and block comment that is not one of those test markers. Build scripts (`*.kts`) are exempt in
`.editorconfig`: they are configuration, with no declaration for a KDoc to attach to.

## Return Types

Every function and method **must** declare an explicit return type, **except** when the return type is `Unit` — in that case the return type may be omitted. However, functions that return `Unit` **must** always use a block body (`{ }`), never an expression body (`=`).

```kotlin
// Correct — non-Unit: explicit return type
fun getSong(id: Long): Song { ... }
fun observeAlbum(albumId: Long): Flow<Album> { ... }
suspend fun refreshSongs(): Result<Unit> { ... }
fun buildGreeting(): String { ... }

// Correct — Unit: no return type, block body
fun play(song: Song) {
    player.play(song)
}

// Wrong — non-Unit return type omitted
fun getSong(id: Long) = songsRepository.find(id)
fun buildGreeting() = "Hello"

// Wrong — Unit function using expression body
fun play(song: Song) = player.play(song)
```

This applies to:
- Top-level functions
- Class methods
- Extension functions
- Composable functions (return `Unit` — use block body, omit return type)

The block-body half of this is enforced by the custom ktlint rule
`tunescout-style:unit-function-block-body` (in `tools/ktlint_custom_rules`). Ktlint rules have no type
resolution, so the rule flags the two cases it can prove: an explicit `: Unit` return type paired with an
expression body, and an expression body that delegates to a `Unit`-returning function declared in the same
class or file. A body that calls a `Unit` function from another file (`= println(...)`) is invisible to it —
treat the rule as a net, not a guarantee.

## Expression Body

Non-Unit functions **must** use expression body (`=`) whenever the entire body is a single expression. Reserve block body (`{ return ... }`) for functions that require multiple statements.

```kotlin
// Correct — single expression, use =
fun getSong(id: Long): Song = songsRepository.find(id)

fun buildGreeting(name: String): String = "Hello, $name"

operator fun invoke(albumId: Long): Result<Unit> = try {
    Result.success(repository.fetch(albumId))
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}

// Correct — multiple statements require block body
fun loadAndLog(id: Long): Song {
    val song = songsRepository.find(id)
    Log.d(TAG, "Loaded $song")
    return song
}

// Wrong — block body with a single return
fun getSong(id: Long): Song {
    return songsRepository.find(id)
}

fun buildGreeting(name: String): String {
    return "Hello, $name"
}
```

This applies to:
- Top-level functions
- Class methods
- Extension functions
- `operator fun invoke`

## Function Naming

A function that returns a value **must** be named as a **verb phrase**, and the verb **must** say what
kind of operation it is. A bare noun (`exoPlayer()`, `pendingFlow()`, `searchParams()`) reads like a
property and hides whether the call creates, reads, computes or does I/O.

| Prefix | Use for |
|---|---|
| *(none — declare a `val`)* | no parameters and no real computation: it is not a function |
| `get…` | cheap in-memory read that takes a parameter |
| `fetch…` / `load…` | I/O — network, disk, database |
| `observe…` | returns a `Flow` you subscribe to |
| `create…` / `build…` | produces a **new** instance |
| `find…` | lookup that may miss (pair with `…OrNull`) |
| `to…` / `as…` | converts between representations |
| `is…` / `has…` / `can…` | returns `Boolean` |

```kotlin
// Correct
fun createExoPlayer(context: Context): ExoPlayer
fun observeRecentlyPlayed(): Flow<List<Song>>
fun getTrackCount(excludingId: Long?): Int
val deviceName: String

// Wrong
fun exoPlayer(context: Context): ExoPlayer          // creates a new instance — 'get' would lie too
fun recentlyPlayedFlow(): Flow<List<Song>>          // noun; say what it does with the flow
fun trackCount(excludingId: Long?): Int             // noun
fun deviceName(): String                            // no params, no computation — should be a val
```

Note that `get` is **not** a catch-all. In Kotlin a parameterless `getX()` should be a property, and
`get` on a factory would claim the instance already exists.

This is enforced automatically by the custom ktlint rule
`tunescout-style:value-returning-function-naming` (in `tools/ktlint_custom_rules`), which flags any
function with a non-`Unit` return type whose name does not start with an approved verb.

**Exempt** (the rule skips these):
- `@Composable` functions that return a value — the Compose API guidelines deliberately name these as
  nouns (`albumArtwork()`, `durationText()`, `mainContentBottomInset()`).
- Extension functions on `Modifier` (`Modifier.shimmer()`, `Modifier.verticalScrollbar()`).
- `override` declarations — the convention is enforced at the interface site.
- `operator` functions, and names ending in `…OrNull` / `…Of` / `…For`.
- Members of a `@Dao` or `@Database` type — Room dictates `songDao()`, `albumDao()`, etc.
- Test source sets — fixtures named after what they produce (`song()`, `songDto()`, `album()`) read
  well in Given/When/Then, so the rule is disabled there in `.editorconfig`.

New verbs are added to `ALLOWED_VERB_PREFIXES` in the rule when a genuinely new kind of operation shows up.

## Imports

Never use fully-qualified (inline) type or function references in the body of a function or expression. Always add the import at the top of the file and use the simple name.

```kotlin
// Correct — import declared, simple name used in body
import org.koin.core.module.Module

fun KoinApplication.registerModules() {
    Module.bindSongsRepository()
    Module.bindSomethingElse()
}

// Wrong — fully-qualified inline reference
fun KoinApplication.registerModules() {
    org.koin.core.module.Module.bindSongsRepository()
    org.koin.core.module.Module.bindSomethingElse()
}
```

This applies to:
- Class references
- Extension function receivers
- Companion object and static-style calls
- Type aliases and generic bounds

### No unused imports

Unused imports are a build error. Stock ktlint ships `standard:no-unused-imports` but leaves it off in the
`ktlint_official` code style, so it is turned on explicitly in `.editorconfig`
(`ktlint_standard_no-unused-imports = enabled`). It is autocorrectable — `./scripts/ktlint.sh --format`
strips them. The rule counts KDoc `[Link]` references as usages, so an import that only exists to make a
KDoc link resolve is kept.

Note that commented-out code does not count as a usage. Commenting out a block and leaving its imports
behind fails the build.

### No wildcard imports

Every import names exactly one symbol — `import androidx.compose.foundation.layout.Row`, never
`import androidx.compose.foundation.layout.*`.

This one needs no configuration: `standard:no-wildcard-imports` is enabled by default in the
`ktlint_official` code style. Because `ij_kotlin_packages_to_use_import_on_demand` is left unset in
`.editorconfig`, no package is exempt — not even `java.util.*`.

Unlike the rule above, this one is **not** autocorrectable, since ktlint cannot know which concrete
symbols a wildcard stands for. `--format` will not fix it; expand the import by hand, or let the IDE's
*Optimize Imports* do it.

## Where a `private val` Lives

A `private val` belongs to the **class body** whenever a class in the file can hold it. File scope is
for values that genuinely have no owner, not a default.

```kotlin
// Correct — the empty state belongs to the ViewModel that falls back to it
class AddToPlaylistViewModel(...) : ViewModel() {
    private val emptyUiState = AddToPlaylistUiState(song = null, playlists = emptyList(), newPlaylistName = null)

    val uiState: StateFlow<AddToPlaylistUiState> = combine(
        useCases.observeSong(route.songId),
        useCases.observePlaylists(),
        newPlaylistName,
        ::AddToPlaylistUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)
}

// Wrong — file scope for something only this class ever reads
private val emptyUiState = AddToPlaylistUiState(song = null, playlists = emptyList(), newPlaylistName = null)

class AddToPlaylistViewModel(...) : ViewModel() { ... }
```

**Declare it before the property that reads it.** A class body initializes top to bottom, so a
`private val` declared *after* the `val` whose initializer uses it is still `null` at that point and
the class throws on construction. Keep these declarations at the top of the body.

File scope stays right in four cases:

- **Compose sizing and styling constants** — the `Dp`, `Shape`, `Color` and `TextUnit` values above
  the composable they size. A `@Composable` function is not a class, and one instance per file is the
  point. This is the convention the next two sections describe.
- **A value the instance cannot see yet** — a default for a constructor parameter
  (`holdDuration: Duration = defaultHoldDuration`) or an argument to a superclass constructor call is
  evaluated before the class body exists, so it cannot read a class-body property.
- **Files with no class** — a Koin module, a fixture file, a theme.
- **`value class` bodies**, which cannot declare properties at all (`Artwork`'s `sizeSegment` regex).

## Companion Objects Hold Constants, Not State

A companion object is for `const val` and for values that are part of the type's public API
(`PlaybackState.Idle`, a JUnit `@RegisterExtension` that has to be static). The class's own private
values go in the class body — putting them in the companion hides instance state in a singleton and
reads like a namespace that is not one.

```kotlin
// Correct
class KtorSongSearchRemoteDataSource(...) {
    private companion object {
        const val SEARCH_PATH = "search"
    }
}

// Wrong — a private val hidden in a companion
class SplashViewModel(...) {
    private companion object {
        val defaultHoldDuration = 700.milliseconds
    }
}
```

The `Wrong` example above is the constructor-default case from the previous section: it cannot move
into the class body, so it moves to file scope instead.

Enforced by two custom ktlint rules. `tunescout-style:companion-object-constants` flags a non-`const`
property that is private, or that sits in a `private companion object`.
`tunescout-style:top-level-val-ownership` flags the opposite mistake — a top-level `private val` or
`private const val` that belongs in a type — and reports where it should go: a `const val` into that
type's `private companion object`, anything else into its class body.

The ownership rule decides by **where the value is read**, which is what lets it keep the four
exceptions above without any type resolution:

- it only fires when *every* reference sits inside one top-level class or object, so a `Dp` a
  top-level `@Composable` sizes itself with is left alone, and so is a constant two top-level
  declarations share;
- a reference from the primary constructor or the superclass constructor call does not count as
  ownership, which covers the `holdDuration: Duration = defaultHoldDuration` case;
- a file with no class has no owner to move anything into;
- interfaces, annotation classes and `value class` bodies are skipped, since they cannot hold the
  property anyway.

## Naming File-Scoped Constants

A `private val` declared at file (top-level) scope — typically right above the `@Composable` function it sizes/shapes for, e.g. a `Dp`, `Shape`, or `TextUnit` constant — **must** use `lowerCamelCase`, never `PascalCase` or `SCREAMING_SNAKE_CASE`.

```kotlin
// Correct
private val buttonMinHeight = 44.dp

@Composable
internal fun MyButton(...) { ... }

// Wrong
private val ButtonMinHeight = 44.dp
private val BUTTON_MIN_HEIGHT = 44.dp
```

This does **not** apply to:
- Actual constants: `private const val` at companion-object scope stays `UPPER_SNAKE_CASE` (see [dry.md](dry.md) / no-constant-value-params convention).
- Public `CompositionLocal` instances (e.g. `val LocalSnackbarHostState = ...`) and public singleton-like objects (e.g. `val NoClip = object : ...`) — these keep `PascalCase`, following standard Compose/Kotlin convention for object-like values.

This is enforced automatically: the custom ktlint rule `tunescout-style:private-top-level-val-naming` (in `tools/ktlint_custom_rules`) fails the build on a violation. Stock ktlint's `standard:property-naming` rule permits `PascalCase` for this kind of file-scoped `Dp`/`Color`-typed `val` (it treats them as "constant-like"), which is why a dedicated rule was needed.

## Positioning File-Scoped Constants

Every top-level `private val` or `private const val` **must** be declared before the first top-level function/class/object in the file — i.e. right after the imports — never after the functions that use it. This applies to both the `Dp`/`Shape`/etc. "constant-like" `val`s above and to plain `private const val` magic numbers.

```kotlin
// Correct
import androidx.compose.ui.unit.dp

private val buttonMinHeight = 44.dp
private const val ANIMATION_MILLIS = 300

@Composable
internal fun MyButton(...) { ... }

// Wrong — declared after the function that uses it
import androidx.compose.ui.unit.dp

@Composable
internal fun MyButton(...) { ... }

private val buttonMinHeight = 44.dp
private const val ANIMATION_MILLIS = 300
```

This is enforced automatically by the custom ktlint rule `tunescout-style:top-level-val-position` (same module as above).

## Blank Lines Between File-Scoped Constants

Consecutive top-level `private val` / `private const val` declarations are written as one block, with **no** blank line between them. The blank line goes only between the block and the first function/class that follows it.

```kotlin
// Correct
private val artworkCornerRadius = 10.dp
private val artworkSize = 44.dp
private val rowSpacing = 18.dp

@Composable
internal fun SongRow(...) { ... }

// Wrong — blank lines scattering one block of constants
private val artworkCornerRadius = 10.dp

private val artworkSize = 44.dp

private val rowSpacing = 18.dp
```

Enforced by the custom ktlint rule `tunescout-style:top-level-val-blank-line`. It is scoped to *private* top-level properties — public top-level `val`s (e.g. the color palettes in `ui/theme`) may keep blank lines between groups.

## When-Entry Bodies

A `when` entry whose body is a single statement that fits on one line **must not** be wrapped in `{ }` braces.

```kotlin
// Correct
when (status) {
    Status.LOADING -> ProgressIndicator()
    Status.ERROR -> ErrorState()
}

// Wrong
when (status) {
    Status.LOADING -> {
        ProgressIndicator()
    }
    Status.ERROR -> {
        ErrorState()
    }
}
```

If `condition -> statement` would exceed the 120-char line limit, wrap the statement onto its own indented line instead of adding braces:

```kotlin
PlaybackState.Unavailable ->
    stringResource(R.string.player_preview_unavailable_message)
```

Braces stay when a branch is genuinely multi-statement, or when the branch's sole statement is itself a bare lambda literal (removing the outer braces there would change meaning — the outer `{ }` is the required block syntax, not optional wrapping).

Because this is the opposite of stock ktlint's `standard:when-entry-bracing` (which forces braces onto *every* entry in a `when` if *any* entry needs them), that stock rule is disabled in `.editorconfig` (`ktlint_standard_when-entry-bracing = disabled`) in favor of the custom rule `tunescout-style:when-entry-single-statement-braces` (in `tools/ktlint_custom_rules`).

## fun interface

Every Kotlin `interface` with exactly one abstract method (and no abstract properties) **must** be declared as `fun interface` (a functional/SAM interface), so callers can pass a lambda instead of an anonymous object.

```kotlin
// Correct
fun interface GetSongUseCase {
    suspend operator fun invoke(id: Long): Song
}

// Wrong
interface GetSongUseCase {
    suspend operator fun invoke(id: Long): Song
}
```

Exception: if the single abstract method has a parameter with a default value, it **cannot** be a `fun interface` (Kotlin forbids default parameter values on a SAM's abstract method) — leave it as a plain `interface`.

Enforced by the custom ktlint rule `tunescout-style:fun-interface-required`.

### No redundant SAM constructors

When passing a lambda as an argument whose parameter type is a `fun interface`, pass the lambda directly — don't wrap it in the interface's name (the "SAM constructor").

```kotlin
// Correct
someUseCaseConsumer(onResult = { song -> ... })

// Wrong — redundant SAM constructor
someUseCaseConsumer(onResult = GetSongUseCase { song -> ... })
```

Best-effort enforcement by the custom ktlint rule `tunescout-style:redundant-sam-constructor-argument`. Note this rule can only see fun interfaces declared in the *same file* as the call site (ktlint rules don't do cross-file type resolution), so it won't catch every case — treat it as a net, not a guarantee, when reviewing code that constructs a fun interface from a different module than where it's declared.

## Interface and Implementation in Separate Files

An interface and a class that implements it never share a file. The interface is the contract a
consumer imports; the implementation is a detail that consumer should not have to scroll past — and
in an `api`/`impl` module split they cannot even live in the same module. One file per type also keeps
the file name honest: `MediaItemFactory.kt` holds `MediaItemFactory`, nothing else.

```kotlin
// Correct — MediaItemFactory.kt
internal fun interface MediaItemFactory {
    fun createMediaItem(entry: QueueEntry): MediaItem
}

// Correct — AndroidMediaItemFactory.kt
internal class AndroidMediaItemFactory : MediaItemFactory { ... }

// Wrong — both in MediaItemFactory.kt
internal fun interface MediaItemFactory { ... }

internal class AndroidMediaItemFactory : MediaItemFactory { ... }
```

Three shapes are exempt, because the implementation genuinely belongs beside the contract:

- a `sealed interface` and its cases — the hierarchy *is* the file (`AlbumUiState` and its `Loading`,
  `Loaded`, `Error`);
- a `private` interface — it cannot be seen from any other file, so its implementation has nowhere
  else to go;
- an implementation nested inside the interface itself, such as a `companion object` default.

Enforced by the custom ktlint rule `tunescout-style:interface-implementation-separate-files`. Ktlint has
no type resolution, so the rule matches the implementation's supertype list against the *names* of the
interfaces declared in the same file — which is exactly the case it exists to catch.

## Method References

When a lambda exists only to hand its parameter to a function, pass the function reference instead.

```kotlin
// Correct
songs = albumDto.tracks.map(::mapTrack)

// Wrong — the lambda adds nothing but a name
songs = albumDto.tracks.map { trackDto -> mapTrack(trackDto) }
```

A reference cannot always replace the lambda. It has no way to carry a `suspend` modifier (`::save` has
type `suspend (T) -> Unit`, which does not fit the plain `(T) -> Unit` that `let`, `map` and `forEach`
declare), `@Composable` functions cannot be referenced at all, and a function reached through a receiver
needs that receiver in the reference (`repository::delete`, `this::isSongPlayed`). In those cases the
lambda stays.

For a nested class's constructor, the reference is the *simple* name plus an import — `Outer::Nested`
does not compile when `Outer` is generic, because `Outer::x` is read as a member reference that wants its
type argument:

```kotlin
import com.pierre.tunescout.core.model.Outer.Nested

value = selectedSong?.title?.uppercase()?.let(::Nested) ?: Outer.Empty
```

Partially enforced by the custom ktlint rule `tunescout-style:prefer-method-reference`. Ktlint resolves
no types, so the rule only fires when the called function is declared in the *same file* — there it can read
the declaration and rule out the `suspend`, `@Composable` and receiver cases above. A forwarding lambda
around a function from another file (including the `Nested` example) is left alone rather than guessed at:
treat the rule as a net for the cases it can prove, not as the definition of the convention.

## Explicit Backing Fields

When a property exposes a read-only view of mutable state, declare the mutable instance as the property's
explicit backing field instead of pairing a `_name` backing property with a public one.

```kotlin
// Correct
val uiState: StateFlow<PlayerUiState>
    field = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)

fun refresh() {
    uiState.update { PlayerUiState.Loading }
}

// Wrong — two declarations for one piece of state
private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
```

Inside the class the property smart-casts to the field's type, so `update`, `value =` and `emit` work on it
directly; outside it is only the declared read-only type. Keep the type argument on the constructor: the field
is inferred from its initializer, so `MutableStateFlow(PlayerUiState.Loading)` would become a
`MutableStateFlow<PlayerUiState.Loading>` and reject every other state. The field works on `override val` too, but
not on `var`, `open`, delegated properties or properties with a custom getter. A `Channel` exposed through
`receiveAsFlow()` is not a subtype of the flow it exposes, so it keeps a private backing property.

Enforced by the custom ktlint rule `tunescout-style:explicit-backing-field`, which flags a private `_name`
`val` whose only purpose is to be exposed as `name` (directly, or through `asStateFlow()`/`asSharedFlow()`).

## Top-Level Functions Need an Owner

A function declared at file scope has no owner: nothing says which class it serves, and nothing can
replace it in a test. Logic belongs to a **class with a clear responsibility** — a factory, a mapper —
that is registered in Koin and injected where it is used.

```kotlin
// Wrong — loose functions next to the type they build
internal fun buildTimeline(songs: List<Song>, startSongId: Long, createEntryId: () -> String): QueueTimeline
internal fun getCarriedEntries(entries: List<QueueEntry>, currentIndex: Int): List<QueueEntry>

// Correct — one injected class owns them, and its own dependency replaces the lambda parameter
internal class QueueTimelineFactory(
    private val idGenerator: IdGenerator,
) {
    fun buildTimeline(songs: List<Song>, startSongId: Long, carriedEntries: List<QueueEntry>): QueueTimeline
    fun getCarriedEntries(entries: List<QueueEntry>, currentIndex: Int): List<QueueEntry>
}

internal class PlaybackQueue(
    private val timelineFactory: QueueTimelineFactory,
)
```

A function that only forwards its arguments to a constructor is not needed at all: pass the constructor
reference (`combine(a, b, c, ::AddToPlaylistUiState)`).

File scope stays right in four cases:

- **`@Composable` functions**, which are not class members by nature.
- **Extension functions** — the receiver is the owner (`List<NavKey>.isMiniPlayerAllowed()`,
  `Playlist.toUiModel()`). Prefer this to a function that takes its subject as the first parameter.
- **`inline` functions**, which are control flow rather than a collaborator (`suspendRunCatching`).
- **`private` helpers that top-level code calls** — the helper of a composable in the same file. A
  `private` function that only one class of the file calls is that class's method.

`:ui:*` cannot be injected into and cannot see `:core:*`, so a value type there builds itself from
primitives through a factory on its companion object: `PlayButtonState.of(isPlaying, hasEnded)`,
`SongSharedKey.createOrNull(songId, element)`.

This is enforced by the custom ktlint rule `tunescout-style:top-level-function-ownership`. Test source
sets and `core/testing` are exempt in `.editorconfig`: fixtures (`song()`, `playbackState()`) are
top-level on purpose.

## Constructor Properties That Could Be Parameters

A primary-constructor `private val` that is only read while the instance is being built — by a property
initializer, a delegate or an `init` block — never needed to be a property. Drop `private val`: a plain
parameter reaches the same places and the class keeps one field less.

```kotlin
// Wrong — a field kept for a delegate that is resolved once
internal class HomeTabsState(
    private val selectedIndexState: MutableIntState,
) {
    private var selectedIndex by selectedIndexState
}

// Correct
internal class HomeTabsState(
    selectedIndexState: MutableIntState,
) {
    private var selectedIndex by selectedIndexState
}
```

The same goes for a ViewModel dependency that only feeds the `uiState` initializer
(`observablePlayback: ObservablePlayback`). A read from a method, a property getter or a nested class
keeps the property.

This is enforced by the custom ktlint rule `tunescout-style:redundant-private-constructor-property`.
It has no type resolution, so it reports only what it can prove: any read from a method, an accessor, a
nested class or through a qualifier (`this.x`) keeps the property.

### Properties first, plain parameters last

In a primary constructor every `val` / `var` comes before the plain parameters. What the instance keeps
reads as one block, and what it only consumes while being built as another:

```kotlin
// Wrong — a plain parameter in the middle of the properties
class AlbumViewModel(
    private val route: AlbumRoute,
    private val useCases: AlbumUseCases,
    observablePlayback: ObservablePlayback,
    private val navigator: Navigator,
) : ViewModel()

// Correct
class AlbumViewModel(
    private val route: AlbumRoute,
    private val useCases: AlbumUseCases,
    private val navigator: Navigator,
    observablePlayback: ObservablePlayback,
) : ViewModel()
```

So when the rule above turns a `private val` into a plain parameter, the parameter also moves to the end.
Call the constructor with named arguments (or let Koin resolve it by type) and the order never reaches a
call site.

This is enforced by the custom ktlint rule `tunescout-style:constructor-property-order`. It is not
autocorrected: moving a parameter changes the meaning of every positional call.

## Unused Parameters

Every parameter of a function is read by it. One that is not is a promise the signature does not keep:
each caller has to produce a value that changes nothing, and the next reader assumes it matters. Remove
it, along with the argument at every call site.

This is enforced by the custom ktlint rule `tunescout-style:unused-function-parameter`. Functions whose
signature is dictated from outside are skipped — `override`, `open`, `abstract`, `operator`,
`expect`/`actual`, `external` and interface members — as is anything annotated with
`@Suppress("UNUSED_PARAMETER")`.

## Composable Naming

A `@Composable` that emits UI ends in a word that says **what kind of UI it is**, taken from a closed
list, so the name alone tells a screen from a row from a side effect:

| Suffix | Use for |
|---|---|
| `Screen` | the entry point of a route: collects the ViewModel state and hands it to a `Content` |
| `Content` | the stateless body of a screen or of one of its states |
| `Scaffold`, `Theme` | structural wrappers that take a `content` slot |
| `Dialog`, `Sheet`, `Card`, `Box`, `Bar`, `Header`, `Row`, `List`, `Grid`, `Cell` | containers and layout pieces |
| `Button`, `Action`, `Toggle`, `Field`, `Handle` | things the user operates |
| `Text`, `Title`, `Heading`, `Label`, `Message`, `Icon`, `Image`, `Artwork`, `Cover`, `Badge`, `Line` | things the user reads or sees |
| `Skeleton` | the loading placeholder of another composable |
| `Effect`, `Collector` | composables that emit no UI and only run a side effect |
| `Component` | anything none of the above describes |

```kotlin
// Correct
@Composable fun NamePromptCard(...)
@Composable fun FollowHideableBarsEffect(...)
@Composable fun PlaybackControlsComponent(...)

// Wrong — a noun that does not say what is drawn
@Composable fun NamePrompt(...)
@Composable fun FollowHideableBars(...)
@Composable fun PlaybackControls(...)
```

The file is named after its main composable, so it carries the suffix too.

Not held to the list:

- **Composables that return a value** (`rememberBarSong`, `songCountText`). They are lowercase and follow
  [Function Naming](#function-naming) and the Compose guidelines instead.
- **`@Preview` functions**, named after what they preview.

This is enforced by the custom ktlint rule `tunescout-style:composable-naming-suffix`. A genuinely new
kind of UI gets a new suffix in `ALLOWED_SUFFIXES` in the rule and a row in the table above — not a
one-off exception.
