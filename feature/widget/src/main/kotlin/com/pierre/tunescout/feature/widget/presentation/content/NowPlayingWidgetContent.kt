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
import com.pierre.tunescout.feature.widget.presentation.component.WidgetSongLabelsComponent
import com.pierre.tunescout.feature.widget.presentation.component.WidgetSurfaceBox
import com.pierre.tunescout.feature.widget.presentation.widget.WidgetContent

private val artworkSize = 40.dp
private val artworkSpacing = 10.dp
private val progressSpacing = 4.dp

/**
 * The one-row widget: the cover, what is playing and the transport on a single line, with the
 * progress under them. One cell of height is barely more than the transport needs, which is why
 * the cover here is smaller than the one the taller widget draws.
 */
@Composable
internal fun NowPlayingWidgetContent(content: WidgetContent) {
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
                    contentDescription = null,
                )
                Spacer(modifier = GlanceModifier.width(artworkSpacing))
                WidgetSongLabelsComponent(
                    song = state.song,
                    modifier = GlanceModifier.defaultWeight(),
                )
                WidgetControlsRow(state = state)
            }
            Spacer(modifier = GlanceModifier.height(progressSpacing))
            WidgetProgressRow(state = state)
        }
    }
}
