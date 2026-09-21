package com.pierre.tunescout.ui.utils.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

data class SharedElementScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

/**
 * The one `SharedTransitionLayout` of the app, provided around everything that can take part in a
 * transition: the `NavDisplay` and the mini player bar beside it.
 */
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?> = compositionLocalOf { null }

/**
 * Both halves a shared element needs, paired with the visibility scope of whoever is drawing it: the
 * `NavDisplay` entry for a screen, its own `AnimatedVisibility` for the mini player.
 *
 * `null` outside a `SharedTransitionLayout`, which is what lets every `*Content` composable keep
 * rendering under `androidTest` and `:tools:screenshots`, where there is no `NavDisplay` at all.
 */
val LocalSharedElementScopes: ProvidableCompositionLocal<SharedElementScopes?> = compositionLocalOf { null }

/**
 * The two places one song can be drawn at the same time: the song that is playing has a row in the
 * list and the mini player bar. A key may only be flown by one of them, and the one that flies is
 * the one the finger landed on.
 */
enum class SharedArtworkSurface {
    LIST_ROW,
    MINI_PLAYER,
}

@Stable
class TappedSharedArtworkSurface {
    var surface: SharedArtworkSurface? by mutableStateOf(null)
}

/**
 * Which surface the subtree being drawn belongs to. `null` for the player, which is the other end of
 * every flight and so never has to yield.
 */
val LocalSharedArtworkSurface: ProvidableCompositionLocal<SharedArtworkSurface?> = compositionLocalOf { null }

/** The surface the finger last landed on, written by whoever handles that tap. */
val LocalTappedSharedArtworkSurface: ProvidableCompositionLocal<TappedSharedArtworkSurface> =
    compositionLocalOf { TappedSharedArtworkSurface() }

@Composable
fun rememberTappedSharedArtworkSurface(): TappedSharedArtworkSurface = remember { TappedSharedArtworkSurface() }

/**
 * The player's end of every flight, for as long as it is drawn. The `NavDisplay` only turns its
 * transition around a few frames after the back stack changes, and until it does the player still
 * counts as the target of its keys. A bar that came back inside that gap would claim the same keys as
 * a second target, and the flight home would land before it had started.
 */
@Stable
class SharedArtworkDestination {
    /** The transition of the entry drawing the player, or null while there is none. */
    var transition: Transition<EnterExitState>? by mutableStateOf(null)

    /** Whether the player is on screen and the `NavDisplay` has not started taking it away. */
    val isStaying: Boolean
        get() = transition?.targetState == EnterExitState.Visible
}

/** The player declares itself here, and the mini player bar waits for it to start leaving. */
val LocalSharedArtworkDestination: ProvidableCompositionLocal<SharedArtworkDestination> =
    compositionLocalOf { SharedArtworkDestination() }

@Composable
fun rememberSharedArtworkDestination(): SharedArtworkDestination = remember { SharedArtworkDestination() }

/**
 * Declares the screen calling it as the [SharedArtworkDestination], until it leaves the composition.
 * A no-op outside a `NavDisplay`, where there is no transition to report.
 */
@Composable
fun SharedArtworkDestinationEffect() {
    val destination = LocalSharedArtworkDestination.current
    val transition = LocalSharedElementScopes.current?.animatedVisibilityScope?.transition ?: return
    DisposableEffect(destination, transition) {
        destination.transition = transition
        onDispose {
            if (destination.transition === transition) {
                destination.transition = null
            }
        }
    }
}

@Composable
fun rememberSharedElementScopes(animatedVisibilityScope: AnimatedVisibilityScope): SharedElementScopes? {
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return null
    return remember(sharedTransitionScope, animatedVisibilityScope) {
        SharedElementScopes(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}

@Composable
private fun isSharedArtworkSurfaceFlying(): Boolean {
    val surface = LocalSharedArtworkSurface.current ?: return true
    return surface == LocalTappedSharedArtworkSurface.current.surface
}

@Composable
fun Modifier.sharedArtwork(key: Any?): Modifier {
    val scopes = LocalSharedElementScopes.current
    if (key == null || scopes == null || !isSharedArtworkSurfaceFlying()) return this
    return with(scopes.sharedTransitionScope) {
        this@sharedArtwork.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = scopes.animatedVisibilityScope,
        )
    }
}

@Composable
fun Modifier.sharedTextBounds(key: Any?): Modifier {
    val scopes = LocalSharedElementScopes.current
    if (key == null || scopes == null || !isSharedArtworkSurfaceFlying()) return this
    return with(scopes.sharedTransitionScope) {
        this@sharedTextBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = scopes.animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.CenterStart,
            ),
        )
    }
}

@Composable
fun isSharedTransitionActive(): Boolean =
    LocalSharedElementScopes.current?.sharedTransitionScope?.isTransitionActive == true
