package com.pierre.tunescout.feature.widget.domain.model

import com.pierre.tunescout.core.model.Song

/**
 * Everything the home screen widgets draw. The player republishes its state four times a second
 * while a song plays and none of that is visible on a widget, so what does not fit here — the
 * position, the queue, the repeat mode — is deliberately left out: two states that are equal are
 * two redraws the launcher never has to do.
 *
 * @property song the song the player is on, kept while it is paused or finished so the widget
 * still has something to show.
 * @property isPlaying whether that song is playing right now.
 * @property hasPrevious whether the queue has a song before the current one.
 * @property hasNext whether the queue has a song after the current one.
 * @property shortcuts the last songs that were played, newest first.
 */
data class WidgetState(
    val song: Song?,
    val isPlaying: Boolean,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val shortcuts: List<Song>,
) {
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
            shortcuts = emptyList(),
        )
    }
}
