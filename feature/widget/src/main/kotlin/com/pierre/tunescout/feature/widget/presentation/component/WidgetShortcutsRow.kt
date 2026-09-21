package com.pierre.tunescout.feature.widget.presentation.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.width
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.widget.presentation.widget.PlayShortcutAction

private val shortcutSize = 54.dp
private val shortcutSpacing = 8.dp

/**
 * The songs listened to last, as covers that start playing where they left off.
 *
 * @param artworks the cover of each song in [songs], in the same order; a shorter list just leaves
 * the last tiles on their placeholder.
 */
@Composable
internal fun WidgetShortcutsRow(
    songs: List<Song>,
    artworks: List<Bitmap?>,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        songs.forEachIndexed { index, song ->
            if (index > 0) {
                Spacer(modifier = GlanceModifier.width(shortcutSpacing))
            }
            WidgetShortcutCell(song = song, artwork = artworks.getOrNull(index))
        }
    }
}

@Composable
private fun WidgetShortcutCell(
    song: Song,
    artwork: Bitmap?,
) {
    WidgetArtworkImage(
        artwork = artwork,
        size = shortcutSize,
        contentDescription = song.title,
        modifier = GlanceModifier.clickable(
            actionRunCallback<PlayShortcutAction>(actionParametersOf(PlayShortcutAction.SONG_ID to song.id)),
        ),
    )
}
