package com.pierre.tunescout.feature.widget.domain.model

import com.pierre.tunescout.core.model.Song
import kotlin.time.Duration

/**
 * Everything the home screen widgets draw.
 *
 * [elapsed] is rounded down to the second it falls in. The player republishes four times a second
 * and a widget has no animation to fill the gaps: every distinct state is a `RemoteViews` the
 * launcher has to redraw, so the widget moves once a second — which is as often as its own clock
 * changes anyway.
 *
 * @property song the song the player is on, kept while it is paused or finished so the widget
 * still has something to show.
 * @property isPlaying whether that song is playing right now.
 * @property hasPrevious whether the queue has a song before the current one.
 * @property hasNext whether the queue has a song after the current one.
 * @property elapsed how far into [song] the player is, to the second.
 * @property total how long [song] is.
 * @property shortcuts the last songs that were played, newest first.
 */
data class WidgetState(
    val song: Song?,
    val isPlaying: Boolean,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val elapsed: Duration,
    val total: Duration,
    val shortcuts: List<Song>,
) {
    /**
     * Derived rather than stored, so it stays out of the equality that decides whether the
     * launcher is asked to redraw.
     */
    val progress: Float
        get() = if (total <= Duration.ZERO) 0f else (elapsed / total).toFloat().coerceIn(0f, 1f)

    companion object {
        /**
         * How many songs the larger widget offers as shortcuts. It is also how far back
         * a tapped shortcut is looked up, so the two can never disagree.
         */
        const val SHORTCUT_COUNT: Int = 5

        val Empty: WidgetState = WidgetState(
            song = null,
            isPlaying = false,
            hasPrevious = false,
            hasNext = false,
            elapsed = Duration.ZERO,
            total = Duration.ZERO,
            shortcuts = emptyList(),
        )
    }
}
