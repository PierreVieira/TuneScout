package com.pierre.tunescout.ui.utils.scroll

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * Turns the direction a list is travelling into the bars being shown or hidden: content moving up
 * takes them away, content moving back down brings them in.
 *
 * The two directions read different numbers. Hiding waits for what the list actually consumed, so a
 * list with nothing to scroll never takes a header away. Showing takes what the gesture offered,
 * because the room a collapsed header gave back is room the list never had to scroll through — a
 * list resting at its top consumes nothing, and reading only the consumed offset would leave the
 * bars stranded off screen.
 *
 * [toggleDistance] is how far the content has to travel after a change of direction before the bars
 * follow, which keeps a finger wavering over the turning point from flipping them on every frame.
 */
internal class HideableBarsNestedScrollConnection(
    private val state: HideableBarsState,
    private val toggleDistance: Float,
) : NestedScrollConnection {
    private var scrolledSinceTurn = 0f

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (available.y > 0f) {
            accumulate(available.y)
        }
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (consumed.y < 0f) {
            accumulate(consumed.y)
        }
        return Offset.Zero
    }

    private fun accumulate(delta: Float) {
        if ((delta > 0f) != (scrolledSinceTurn > 0f)) {
            scrolledSinceTurn = 0f
        }
        scrolledSinceTurn += delta
        if (scrolledSinceTurn >= toggleDistance) {
            state.show()
            scrolledSinceTurn = 0f
        } else if (scrolledSinceTurn <= -toggleDistance) {
            state.hide()
            scrolledSinceTurn = 0f
        }
    }
}
