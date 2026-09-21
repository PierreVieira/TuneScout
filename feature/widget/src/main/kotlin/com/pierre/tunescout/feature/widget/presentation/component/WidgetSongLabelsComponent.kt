package com.pierre.tunescout.feature.widget.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.layout.Column
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.widget.R

private val titleFontSize = 15.sp
private val subtitleFontSize = 13.sp

/**
 * The title and the artist of the song on the widget, or the invitation to open the app when
 * nothing has been played yet.
 */
@Composable
internal fun WidgetSongLabelsComponent(
    song: Song?,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(
            text = song?.title ?: context.getString(R.string.widget_empty_title),
            style = TextStyle(
                color = WidgetColors.textPrimary,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
        )
        Text(
            text = song?.artistName ?: context.getString(R.string.widget_empty_message),
            style = TextStyle(color = WidgetColors.textSecondary, fontSize = subtitleFontSize),
            maxLines = 1,
        )
    }
}
