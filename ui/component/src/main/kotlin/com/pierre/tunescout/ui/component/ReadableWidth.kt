package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val maxReadableWidth = 480.dp

/**
 * Caps a list or a column of text near the width a phone gives it in portrait, which is what its
 * rows were laid out for. A landscape phone is wide enough to stretch a song row across the screen
 * and strand its content on the left; the parent centres what is left over.
 *
 * The cap comes before the fill: the other order hands `widthIn` a minimum that is already the
 * parent's width, which it cannot go below, and nothing is capped at all.
 */
fun Modifier.readableWidth(): Modifier = widthIn(max = maxReadableWidth).fillMaxWidth()
