package com.pierre.tunescout.feature.widget.presentation.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.feature.widget.domain.model.WidgetState
import kotlinx.coroutines.CancellationException

internal class WidgetArtworkLoader(
    private val imageLoader: ImageLoader,
    private val context: Context,
) {
    /**
     * @return [state] with every artwork it names decoded, each one `null` when it could not be
     * fetched — the widget then draws its placeholder rather than nothing at all.
     */
    suspend fun loadContent(state: WidgetState): WidgetContent = WidgetContent(
        state = state,
        songArtwork = state.song?.let { song -> loadBitmapOrNull(song.artwork) },
        shortcutArtworks = state.shortcuts.map { song -> loadBitmapOrNull(song.artwork) },
    )

    /**
     * The thumbnail is asked for rather than the full cover: a widget draws it at a few dozen
     * pixels, and every bitmap in a `RemoteViews` is copied across process boundaries.
     *
     * Hardware bitmaps are turned off for the same reason — the launcher's process cannot read
     * one, and the widget would come out blank.
     *
     * @return the decoded cover, or `null` when it could not be fetched.
     */
    private suspend fun loadBitmapOrNull(artwork: Artwork): Bitmap? {
        val request = ImageRequest
            .Builder(context)
            .data(artwork.thumbnailUrl)
            .allowHardware(false)
            .build()
        return try {
            (imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Could not load the widget artwork ${artwork.thumbnailUrl}: ${exception.message}")
            null
        }
    }

    private companion object {
        const val TAG = "WidgetArtworkLoader"
    }
}
