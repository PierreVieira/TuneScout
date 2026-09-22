package com.pierre.tunescout.core.playback.internal

import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.request.ImageRequest
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Wraps [delegate] to also warm the artwork disk cache for a song added for download.
 *
 * Every list in the app draws a song's [Artwork.thumbnailUrl] or [Artwork.mediumUrl], so those
 * variants tend to already be cached by the time a song is downloaded. The player draws
 * [Artwork.largeUrl], a size nothing else requests — it is only fetched the first time the full
 * player screen opens for that song. A song downloaded for offline listening but never opened in
 * the player while still online reaches that screen with no cached large cover and no network to
 * fetch one, so it falls back to the broken-image placeholder even though the same cover loads
 * fine everywhere else.
 *
 * @property delegate what actually fetches the song's audio.
 * @property imageLoader where each artwork variant is fetched into and cached from.
 * @property context passed to each [ImageRequest] built for a prefetch.
 * @property scope where each prefetch runs, so a slow or failing fetch never blocks the audio
 * download this decorates.
 */
internal class ArtworkCachingDownloadCommands(
    private val delegate: DownloadCommands,
    private val imageLoader: ImageLoader,
    private val context: Context,
    private val scope: CoroutineScope,
) : DownloadCommands {
    override fun add(song: Song) {
        delegate.add(song)
        scope.launch { prefetch(song.artwork) }
    }

    override fun remove(songId: Long) = delegate.remove(songId)

    private suspend fun prefetch(artwork: Artwork) {
        listOf(artwork.thumbnailUrl, artwork.mediumUrl, artwork.largeUrl).forEach { url -> fetchOrLog(url) }
    }

    private suspend fun fetchOrLog(url: String) {
        try {
            imageLoader.execute(ImageRequest.Builder(context).data(url).build())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Could not prefetch artwork $url: ${exception.message}")
        }
    }

    private companion object {
        const val TAG = "ArtworkCachingDownloadCommands"
    }
}
