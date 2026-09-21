package com.pierre.tunescout.feature.widget.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.width
import com.pierre.tunescout.feature.widget.presentation.component.WidgetArtworkImage
import com.pierre.tunescout.feature.widget.presentation.component.WidgetControlsRow
import com.pierre.tunescout.feature.widget.presentation.component.WidgetSongLabelsComponent
import com.pierre.tunescout.feature.widget.presentation.component.WidgetSurfaceBox
import com.pierre.tunescout.feature.widget.presentation.widget.WidgetContent

private val artworkSize = 48.dp
private val artworkSpacing = 10.dp

/**
 * The one-row widget: the cover, what is playing and the transport, on a single line.
 */
@Composable
internal fun NowPlayingWidgetContent(content: WidgetContent) {
    val state = content.state
    WidgetSurfaceBox(songId = state.song?.id) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WidgetArtworkImage(
                artwork = content.songArtwork,
                size = artworkSize,
                contentDescription = state.song?.title,
            )
            Spacer(modifier = GlanceModifier.width(artworkSpacing))
            WidgetSongLabelsComponent(
                song = state.song,
                modifier = GlanceModifier.defaultWeight(),
            )
            WidgetControlsRow(state = state)
        }
    }
}
