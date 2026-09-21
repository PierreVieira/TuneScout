package com.pierre.tunescout.feature.widget.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import com.pierre.tunescout.feature.widget.presentation.component.WidgetArtworkImage
import com.pierre.tunescout.feature.widget.presentation.component.WidgetControlsRow
import com.pierre.tunescout.feature.widget.presentation.component.WidgetProgressRow
import com.pierre.tunescout.feature.widget.presentation.component.WidgetShortcutsRow
import com.pierre.tunescout.feature.widget.presentation.component.WidgetSongLabelsComponent
import com.pierre.tunescout.feature.widget.presentation.component.WidgetSurfaceBox
import com.pierre.tunescout.feature.widget.presentation.widget.WidgetContent

private val artworkSize = 68.dp
private val artworkSpacing = 10.dp
private val rowSpacing = 8.dp
private val progressSpacing = 4.dp

/**
 * The two-row widget: the same transport and progress as the smaller one, with the songs listened
 * to last underneath them.
 */
@Composable
internal fun NowPlayingShortcutsWidgetContent(content: WidgetContent) {
    val state = content.state
    WidgetSurfaceBox(songId = state.song?.id) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WidgetArtworkImage(
                    artwork = content.songArtwork,
                    size = artworkSize,
                    contentDescription = state.song?.title,
                )
                Spacer(modifier = GlanceModifier.width(artworkSpacing))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    WidgetSongLabelsComponent(song = state.song)
                    Spacer(modifier = GlanceModifier.height(progressSpacing))
                    WidgetProgressRow(state = state)
                    WidgetControlsRow(state = state)
                }
            }
            Spacer(modifier = GlanceModifier.height(rowSpacing))
            WidgetShortcutsRow(
                songs = state.shortcuts,
                artworks = content.shortcutArtworks,
            )
        }
    }
}
