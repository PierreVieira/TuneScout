package com.pierre.tunescout.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

/**
 * Shorter than the 700 ms the Navigation 3 defaults fade for: the screen behind a shared element has
 * to be out of the way while the element is still flying, not long after it has landed. The default
 * predictive-back spec also scales the outgoing screen down, which drags the element with it.
 */
private const val TRANSITION_DURATION_MILLIS = 350

internal fun AnimatedContentTransitionScope<*>.createFadeTransform(): ContentTransform = ContentTransform(
    targetContentEnter = fadeIn(animationSpec = tween(TRANSITION_DURATION_MILLIS)),
    initialContentExit = fadeOut(animationSpec = tween(TRANSITION_DURATION_MILLIS)),
)
