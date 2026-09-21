package com.pierre.tunescout.core.navigation.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.scene.SceneDecoratorStrategyScope
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.pierre.tunescout.ui.utils.animation.modulatedFade

/**
 * Fades every screen of a `NavDisplay` in and out with [modulatedFade], which has to be paired with
 * [createSceneFadeTransform] as the display's transitions, which run no fade of their own.
 *
 * It is a scene decorator rather than an entry decorator because `NavDisplay` never hands a sheet or
 * a dialog to one. Those are drawn over the `AnimatedContent`, but read the animation scope of the
 * screen under them, so an entry decorator would fade a sheet with whatever screen is changing below.
 *
 * @param T the type of the display's navigation keys.
 */
class FadeSceneDecoratorStrategy<T : Any> : SceneDecoratorStrategy<T> {
    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> = FadingScene(scene)
}

/**
 * The transitions a `NavDisplay` decorated with [FadeSceneDecoratorStrategy] runs: none of their own,
 * since the fade is the decorator's. The screen that leaves still stays until it has faded out: its
 * fade animates on the same transition, which is not over until the fade is. The default
 * predictive-back spec would also scale the outgoing screen down, which drags a shared element with it.
 *
 * @return the transform for every push, pop and predictive pop.
 */
fun AnimatedContentTransitionScope<*>.createSceneFadeTransform(): ContentTransform = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = ExitTransition.None,
)

/**
 * [scene], drawn through [modulatedFade].
 *
 * The key pairs [scene]'s class with its own, because `NavDisplay` tells two scenes apart by class
 * and key and every decorated scene now has this class.
 *
 * @property scene the scene to fade.
 * @param T the type of the display's navigation keys.
 */
private class FadingScene<T : Any>(
    private val scene: Scene<T>,
) : Scene<T> {
    override val key: Any = scene::class to scene.key
    override val entries: List<NavEntry<T>> get() = scene.entries
    override val previousEntries: List<NavEntry<T>> get() = scene.previousEntries
    override val metadata: Map<String, Any> get() = scene.metadata
    override val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier.modulatedFade(
                visibilityScope = LocalNavAnimatedContentScope.current,
                animationSpec = tween(FADE_DURATION_MILLIS),
            ),
            propagateMinConstraints = true,
        ) {
            scene.content()
        }
    }

    override fun equals(other: Any?): Boolean = other is FadingScene<*> && other.scene == scene

    override fun hashCode(): Int = scene.hashCode()

    private companion object {
        /**
         * Shorter than the 700 ms the Navigation 3 defaults fade for: the screen behind a shared
         * element has to be out of the way while the element is still flying, not long after it has
         * landed.
         */
        const val FADE_DURATION_MILLIS = 350
    }
}
