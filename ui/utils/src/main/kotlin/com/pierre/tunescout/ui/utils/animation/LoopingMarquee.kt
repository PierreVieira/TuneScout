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

@Composable
fun Modifier.loopingMarquee(): Modifier = basicMarquee(
    iterations = if (isSharedTransitionActive()) MARQUEE_STOPPED else Int.MAX_VALUE,
    repeatDelayMillis = CONTINUOUS_REPEAT_DELAY_MILLIS,
    initialDelayMillis = MARQUEE_INITIAL_DELAY_MILLIS,
    spacing = MarqueeSpacing(marqueeGap),
)
