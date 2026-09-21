package com.pierre.tunescout.ui.utils.animation

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val ANIMATIONS_REMOVED_SCALE = 0f
private const val DEFAULT_ANIMATOR_SCALE = 1f

/**
 * Whether the user asked the system to remove animations ("Remove animations" under Accessibility,
 * which sets the animator duration scale to zero).
 *
 * Compose already shortens a finite animation to nothing at that scale, so a transition needs no
 * help. What does not stop by itself is motion with no end — a marquee, a shimmer, bouncing bars —
 * and that is what checks this: each falls back to a still frame instead (WCAG 2.2.2).
 *
 * @return true when endless motion should be drawn still.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(
            resolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            DEFAULT_ANIMATOR_SCALE,
        ) == ANIMATIONS_REMOVED_SCALE
    }
}
