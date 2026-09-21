package com.pierre.tunescout.ui.utils.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

private const val HIDDEN_ALPHA = 0f
private const val SHOWN_ALPHA = 1f

/**
 * Fades this content in and out with [visibilityScope], the way `fadeIn` and `fadeOut` would, but
 * without an offscreen buffer.
 *
 * `fadeIn` and `fadeOut` set the alpha of a layer with overlapping content, which the renderer
 * draws into an offscreen buffer of the whole content on every frame of the fade and then blends
 * back. Over a whole screen that buffer was most of the cost of opening and closing the player.
 * [CompositingStrategy.ModulateAlpha] applies the alpha to each draw instead: where two things
 * this content draws overlap, the one below shows through the one above while the fade runs, which
 * nothing drawn over the app's single background makes visible in a fade this short.
 *
 * The alpha animates on [visibilityScope]'s own transition, so the content is kept until it has
 * faded out even when the `AnimatedVisibility` or `AnimatedContent` itself runs no exit
 * transition of its own.
 *
 * @param animationSpec how the alpha moves, by default the spring `fadeIn` and `fadeOut` use.
 * @return this modifier, fading with [visibilityScope].
 */
@Composable
fun Modifier.modulatedFade(
    visibilityScope: AnimatedVisibilityScope,
    animationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
): Modifier {
    val alpha by visibilityScope.transition.animateFloat(
        transitionSpec = { animationSpec },
        label = "modulatedFade",
    ) { state -> if (state == EnterExitState.Visible) SHOWN_ALPHA else HIDDEN_ALPHA }
    return graphicsLayer {
        this.alpha = alpha
        compositingStrategy = CompositingStrategy.ModulateAlpha
    }
}
