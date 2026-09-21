package com.pierre.tunescout.feature.widget.presentation.widget

import android.graphics.Bitmap
import com.pierre.tunescout.feature.widget.domain.model.WidgetState

/**
 * A [WidgetState] with its artwork already decoded. `RemoteViews` cannot load an image itself and
 * a Glance composable cannot suspend, so the bitmaps are fetched while the state is still a flow
 * and travel next to it.
 *
 * @property state what the widget draws.
 * @property songArtwork the artwork of [WidgetState.song], or `null` while it is not in the cache.
 * @property shortcutArtworks the artwork of each [WidgetState.shortcuts] entry, in the same order.
 */
internal data class WidgetContent(
    val state: WidgetState,
    val songArtwork: Bitmap?,
    val shortcutArtworks: List<Bitmap?>,
) {
    companion object {
        val Empty: WidgetContent = WidgetContent(
            state = WidgetState.Empty,
            songArtwork = null,
            shortcutArtworks = emptyList(),
        )
    }
}
