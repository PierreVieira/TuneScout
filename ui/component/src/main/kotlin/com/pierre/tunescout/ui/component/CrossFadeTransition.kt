package com.pierre.tunescout.ui.component

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

private const val CROSS_FADE_MILLIS = 220

/**
 * One pane fading into another, with nothing sliding.
 *
 * It is the motion for a swap between siblings — the tabs of the home display — where a slide would
 * claim a direction, and with it a hierarchy, that the two panes do not have between them.
 */
fun AnimatedContentTransitionScope<*>.createCrossFadeTransition(): ContentTransform =
    fadeIn(animationSpec = tween(CROSS_FADE_MILLIS)) togetherWith
        fadeOut(animationSpec = tween(CROSS_FADE_MILLIS))
