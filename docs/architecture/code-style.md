# Code Style

## Comments

Production code has no comments: no `//`, no `/* */`, no KDoc. A name or a small extracted function
carries the intent instead. The `// Correct` / `// Wrong` markers in the examples below are annotations
for this document, not something to copy into code. Test bodies are the one exception: their
`// Given` / `// When` / `// Then` section markers are intentional structure — see
[docs/testing/given-when-then.md](../testing/given-when-then.md).

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
`tunescout-style:unit-function-block-body` (in `tools/ktlint-custom-rules`). Ktlint rules have no type
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
`tunescout-style:value-returning-function-naming` (in `tools/ktlint-custom-rules`), which flags any
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
class MiniPlayerViewModel(...) : ViewModel() {
    private val emptyUiState = MiniPlayerUiState(song = null, isPlaying = false, progress = 0f)

    val uiState: StateFlow<MiniPlayerUiState> = playbackController.state
        .map(::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)
}

// Wrong — file scope for something only this class ever reads
private val emptyUiState = MiniPlayerUiState(song = null, isPlaying = false, progress = 0f)

class MiniPlayerViewModel(...) : ViewModel() { ... }
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
class KtorITunesRemoteDataSource(...) {
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

Enforced by the custom ktlint rule `tunescout-style:companion-object-constants`, which flags a
non-`const` property that is private, or that sits in a `private companion object`. The class-body
preference above is **not** enforced: telling a Compose `Dp` constant apart from a piece of state
needs type resolution, which ktlint rules do not have. It is a review convention.

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

This is enforced automatically: the custom ktlint rule `tunescout-style:private-top-level-val-naming` (in `tools/ktlint-custom-rules`) fails the build on a violation. Stock ktlint's `standard:property-naming` rule permits `PascalCase` for this kind of file-scoped `Dp`/`Color`-typed `val` (it treats them as "constant-like"), which is why a dedicated rule was needed.

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

Because this is the opposite of stock ktlint's `standard:when-entry-bracing` (which forces braces onto *every* entry in a `when` if *any* entry needs them), that stock rule is disabled in `.editorconfig` (`ktlint_standard_when-entry-bracing = disabled`) in favor of the custom rule `tunescout-style:when-entry-single-statement-braces` (in `tools/ktlint-custom-rules`).

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
