# Architecture overview

MVVM with unidirectional data flow, split into Gradle modules where each feature owns its own
data, domain and presentation layers and `app` wires everything together. The full module tree,
and what goes where inside a module, is in [Module structure](module-structure.md); this page is
the tour.

**Dependency rules are enforced, not hoped for.** Features never depend on features, core never
depends on a feature, `ui` knows nothing about features or data, and only `app` sees features.
`assertModuleGraph` fails the build otherwise. Shared things live in core: models, routes, the
navigator, the playback contract and the history. The network, database and playback layers are
each split into a plain-Kotlin `api` module every feature depends on and an `impl` module only
`app` sees, so an implementation can be swapped without touching a feature.

**Screens.** Every screen is a `UiState` rendered by a stateless `*Content` composable, a
`UiEvent` sealed interface handled by a single `onEvent` in the ViewModel, and a `Navigator`
injected into the ViewModel. Navigation is never a UI side effect the screen has to forward. See
[State management](state-management.md).

**Navigation 3.** Routes are `@Serializable` `NavKey`s in `core/navigation`. ViewModels push
commands into a `Navigator`; one collector in `app` applies them to the back stack through
`BackStackController`, which guards against duplicate pushes and never pops the root. The song
options sheet is a route rendered by a bottom sheet scene strategy, so Songs and Player open it
the same way. See [Navigation](navigation.md).

**Data.** The network layer is an interface with a Ktor implementation kept `internal`; DTOs
are mapped once to domain models and never leave the module. Room stores normalized tables
(songs, albums, history with a foreign key) and exposes flows. Repositories live in the feature
that needs them and combine the two. See [Data sources](data-sources.md).

**Pagination.** The iTunes API ignores `offset` and caps `limit` at 200, so the Paging 3 source
re-requests with a growing limit and keeps only the unseen tail, deduplicating by id. It is not a
cursor, but it is an honest fit for the API, and the Paging load states drive the list UI.

**Playback.** One ExoPlayer instance is shared by the app and by a `MediaSessionService` that
posts the media notification. `ObservablePlayback` publishes a `PlaybackState` every screen reads,
and a small recorder turns "first time a song plays" into a row in the history table. The home
screen widgets in `feature/widget` read the same state and send their taps through the same
controller.

**The queue has two tiers.** `PlaybackState` carries `QueueEntry` items tagged `Context` (the album,
playlist or liked songs playing) or `UserQueue` (added by hand), and the play order is the context
up to the current song, then everything queued by hand, then the rest of the context. Starting
another album keeps what you queued. Adding, removing and reordering mutate the ExoPlayer timeline in place, so touching the
queue never interrupts the song that is playing. A keeper writes the queue, the current entry and
the position to Room — on every change and at most every five seconds while playing — and restores
them, paused and prepared, when the app starts.

The reasoning behind these and other choices, with what each one costs, is in
[Decisions and trade-offs](../decisions.md).
