# Decisions and trade-offs

A running log, newest first. Each entry states the decision, why, and what it costs.

## 2026-09-18 — Splash

**The system splash screen is themed to continue into the app splash.** Since API 31 every cold
start begins with a splash window drawn by the system, and by default it shows the launcher icon
over `?android:colorBackground`. `Theme.TuneScout.Splash` (through `core-splashscreen`, so API 26
behaves the same) replaces that icon with the same note artwork the Compose splash draws, sized
and centred to land on the exact same pixels, and removes the system exit animation so the icon
never jumps or scales. Cost: the note lives in `feature:splash` and the theme in `app` references
it across modules, and the icon size is expressed as a fraction of the 288 dp icon canvas.

**The system splash uses the average colour of the splash artwork.** That window only accepts a
solid colour — a gradient is not a valid value for `windowSplashScreenBackground` — so it is set
to `#000E11`, the mean colour of the designed gradient, which is also its colour at the centre of
the screen where the note sits. The app's own splash then draws the gradient from the Figma file
unchanged and without any fade, so the design is never shown half-rendered. Cost: while the
system window is up, the teal glow at the top is missing; nothing but a platform change can fix
that.

## 2026-09-18 — Loading states

**Skeletons with shimmer instead of spinners.** Every loading state (search results, next page,
player, album) renders the shape of the content it is waiting for, and artwork shimmers until
Coil delivers the bitmap. The effect comes from `compose-shimmer`, but the dependency lives only
in `ui:component` behind `Modifier.shimmer()` and `ShimmerBox`, so a future swap touches one
file. Skeleton containers carry a "Loading" content description for screen readers.

## 2026-09-18 — Data

**Paging by growing limit.** The iTunes Search API has no offset or cursor and caps `limit` at
200. `SearchSongsPagingSource` therefore asks for `delivered + pageSize` items and keeps the tail
it has not delivered yet, dropping any id already seen. Paging 3 still gives the list its load
states, retry and prefetch. Cost: page N re-downloads pages 1..N-1. Mitigated by Ktor's HTTP
cache, which honours the API's one-day `Cache-Control`, so the first page of a repeated search is
served locally and the throttling limit of about 20 calls per minute is rarely touched.

**Offline first where it matters.** The history and every song that ever appeared on screen are
stored in Room, so Player and Album open without a network. Search results are not persisted per
query: the challenge asks for the recently played list to work offline, and caching search pages
would add a table and an eviction policy without changing the experience.

## 2026-09-18 — Playback

**One ExoPlayer, shared by the app and the media service.** `PlaybackController` wraps the
process-wide ExoPlayer directly; `PlaybackService` (a `MediaSessionService`) builds its
`MediaSession` over that same instance and is started when playback begins. The textbook setup
routes every screen through a `MediaController` bound to the service, but that adds an async
connection and IPC for a player that already lives in the same process. Cost: the controller
must be used from the main thread, which ViewModels already guarantee. Benefit: the notification,
lock-screen controls and the in-app player all read one state with no synchronisation code.

**Seeking never pauses.** The seek bar previews the drag locally and calls `seekTo` on release;
ExoPlayer keeps playing from the new position, the way Spotify does.

**Permissions only when needed.** `POST_NOTIFICATIONS` is requested the first time a song
actually starts playing on Android 13+, not at launch. Media controls still work if the user
declines because Android 13+ derives them from the media session, not from the notification.

**Recently played is written in one place.** A recorder observes playback state and stores a
song the first time it plays, whatever screen started it. The history table is capped at 20 rows
inside the same transaction that inserts the new one.

## 2026-09-18 — Foundation

**Multi-module with layers inside each feature.** Every `feature/*` module owns its
`data / domain / presentation` packages; `core/*` holds what more than one feature needs; `app`
wires everything. The alternative of one Gradle module per layer was rejected because it scales by
layer instead of by screen, and a change to one screen then touches three modules. Cost: more
Gradle files. Mitigated by convention plugins in `build-logic`, so a feature's build script is a
handful of lines.

**Dependency rules are checked, not hoped for.** `modules-graph-assert` forbids feature → feature,
core → feature and anything → app. Features talk through routes in `core:navigation` and shared
state in `core:*`.

**Navigation 3 with a `Navigator` event bus.** ViewModels emit navigation commands into a channel;
the single `NavDisplay` in `app` is the only place the back stack is mutated. Navigation is never a
UI side effect the screen has to forward.

**Kotlin-first libraries.** Ktor over Retrofit, kotlinx.serialization over Moshi, Koin over Hilt.
They keep the domain and data layers free of annotation processing and make the network layer easy
to swap: the rest of the app only sees an interface.

**JUnit 6 everywhere, Truth for assertions.** Local unit tests run on JUnit Jupiter through the
JUnit Platform; Compose screen tests and end-to-end flows are instrumented tests on the same
framework through the android-junit5 plugin and its Compose extension. No JUnit 4, no Vintage
engine, no Robolectric. Cost: screen tests need an API 35+ emulator instead of running on the JVM.
Benefit: one modern framework, `suspend` test methods, `@Nested` and `@ParameterizedTest`, and
screens tested on a real Android runtime.

**ktlint with project rules instead of detekt.** Formatting and a handful of conventions
(function naming, explicit backing fields, `fun interface`, `when` bracing) are enforced by a
custom ruleset in `tools/ktlint-custom-rules`, run by the same script locally and in CI.
