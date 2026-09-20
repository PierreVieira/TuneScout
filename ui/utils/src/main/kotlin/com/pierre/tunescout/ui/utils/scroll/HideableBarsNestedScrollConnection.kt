package com.pierre.tunescout.ui.utils.scroll

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

internal class HideableBarsNestedScrollConnection(
    private val state: HideableBarsState,
    private val toggleDistance: Float,
) : NestedScrollConnection {
    private var scrolledSinceTurn = 0f

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        showOnDragOffered(offeredY = available.y)
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        hideOnContentMoved(movedY = consumed.y)
        return Offset.Zero
    }

    private fun showOnDragOffered(offeredY: Float) {
        if (offeredY > 0f) {
            accumulate(offeredY)
        }
    }

    private fun hideOnContentMoved(movedY: Float) {
        if (movedY < 0f) {
            accumulate(movedY)
        }
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
