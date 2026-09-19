package com.pierre.tunescout.core.navigation.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.pierre.tunescout.ui.utils.animation.LocalSharedElementScopes
import com.pierre.tunescout.ui.utils.animation.rememberSharedElementScopes

/**
 * Hands every entry the two scopes a shared element needs, so a screen only has to tag the element
 * itself. The animation scope belongs to the `NavDisplay`'s `AnimatedContent`, which is why it can
 * only be read from inside an entry — and why this is a decorator rather than something each
 * feature's `*Entry.kt` repeats.
 */
@Composable
fun <T : Any> rememberSharedElementNavEntryDecorator(): NavEntryDecorator<T> = remember {
    NavEntryDecorator { entry ->
        CompositionLocalProvider(
            LocalSharedElementScopes provides rememberSharedElementScopes(LocalNavAnimatedContentScope.current),
        ) {
            entry.Content()
        }
    }
}
