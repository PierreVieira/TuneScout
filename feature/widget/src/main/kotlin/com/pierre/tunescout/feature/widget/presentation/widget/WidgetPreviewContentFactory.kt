package com.pierre.tunescout.feature.widget.presentation.widget

import android.content.Context
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.widget.R
import com.pierre.tunescout.feature.widget.domain.model.WidgetState
import kotlin.time.Duration

/**
 * The sample the widget picker is shown.
 *
 * It is deliberately fixed rather than whatever the player happens to be on. A preview is rendered
 * once and the system rate limits how often it takes a new one, so a live one would freeze on the
 * state of that moment — most likely the empty one, since publishing at process start races with
 * the saved queue coming back from the database. The sample is drawn by the real composables, so
 * the picker cannot end up showing a layout the widget no longer has.
 */
internal class WidgetPreviewContentFactory {
    /**
     * @return a widget with a song on it, both skips live, and a full shortcut row. No artwork is
     * supplied: the covers are what the widget fills in from the listener's own library, and the
     * placeholder is what says so.
     */
    fun createContent(context: Context): WidgetContent = WidgetContent(
        state = WidgetState(
            song = createSampleSong(context),
            isPlaying = false,
            hasPrevious = true,
            hasNext = true,
            shortcuts = createSampleShortcuts(context),
        ),
        songArtwork = null,
        shortcutArtworks = emptyList(),
    )

    /**
     * @return one tile per [WidgetState.SHORTCUT_COUNT], so the row is drawn at the width it has
     * in use. They carry distinct ids only because they are songs: nothing in a preview is
     * clickable, so no id is ever looked up.
     */
    private fun createSampleShortcuts(context: Context): List<Song> {
        val sample = createSampleSong(context)
        return List(WidgetState.SHORTCUT_COUNT) { index -> sample.copy(id = SAMPLE_SONG_ID + index) }
    }

    private fun createSampleSong(context: Context): Song = Song(
        id = SAMPLE_SONG_ID,
        title = context.getString(R.string.widget_preview_song_title),
        artistName = context.getString(R.string.widget_preview_song_artist),
        albumId = SAMPLE_SONG_ID,
        albumTitle = "",
        artwork = Artwork(""),
        previewUrl = "",
        duration = Duration.ZERO,
        trackNumber = 0,
    )

    private companion object {
        const val SAMPLE_SONG_ID = 0L
    }
}
