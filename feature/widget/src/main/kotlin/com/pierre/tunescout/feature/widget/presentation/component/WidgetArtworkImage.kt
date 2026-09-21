package com.pierre.tunescout.feature.widget.presentation.component

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import com.pierre.tunescout.feature.widget.R

private val artworkCornerRadius = 10.dp
private val placeholderSize = 20.dp

/**
 * The cover of a song, or the app's note while the bitmap is not in the cache yet.
 */
@Composable
internal fun WidgetArtworkImage(
    artwork: Bitmap?,
    size: Dp,
    contentDescription: String?,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(WidgetColors.surface)
            .cornerRadius(artworkCornerRadius),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork == null) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_song_placeholder),
                contentDescription = contentDescription,
                modifier = GlanceModifier.size(placeholderSize),
                colorFilter = ColorFilter.tint(WidgetColors.elementDisabled),
            )
        } else {
            Image(
                provider = ImageProvider(artwork),
                contentDescription = contentDescription,
                modifier = GlanceModifier.fillMaxSize().cornerRadius(artworkCornerRadius),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
