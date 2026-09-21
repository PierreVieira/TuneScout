package com.pierre.tunescout.ui.utils.animation

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val marqueeGap = 48.dp
private const val MARQUEE_STOPPED = 0
private const val CONTINUOUS_REPEAT_DELAY_MILLIS = 0
private const val MARQUEE_INITIAL_DELAY_MILLIS = 1_200

/**
 * A marquee scrolls at a velocity, so it ignores the system's animator scale and would keep moving
 * for someone who turned animations off. With motion reduced it is not applied at all: the text is
 * measured against its real width again, and the caller's `overflow` ends it in an ellipsis.
 *
 * @return this modifier, scrolling text too long for its line unless motion is reduced.
 */
@Composable
fun Modifier.loopingMarquee(): Modifier = if (rememberReduceMotion()) {
    this
} else {
    basicMarquee(
        iterations = if (isSharedTransitionActive()) MARQUEE_STOPPED else Int.MAX_VALUE,
        repeatDelayMillis = CONTINUOUS_REPEAT_DELAY_MILLIS,
        initialDelayMillis = MARQUEE_INITIAL_DELAY_MILLIS,
        spacing = MarqueeSpacing(marqueeGap),
    )
}
