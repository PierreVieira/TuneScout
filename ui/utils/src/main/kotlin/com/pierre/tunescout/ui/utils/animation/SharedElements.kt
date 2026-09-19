package com.pierre.tunescout.ui.utils.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
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
fun Modifier.sharedArtwork(key: Any?): Modifier {
    val scopes = LocalSharedElementScopes.current
    if (key == null || scopes == null) return this
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
    if (key == null || scopes == null) return this
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
