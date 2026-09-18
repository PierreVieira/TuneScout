# Decisions and trade-offs

A running log, newest first. Each entry states the decision, why, and what it costs.

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
