package com.pierre.tunescout.core.playback.internal

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.ContentMetadata
import com.pierre.tunescout.core.model.Song

/**
 * Reads [cache] the way the player writes it: [MediaCacheDataSourceFactory] keys each preview by its
 * url, and the content length is recorded once the first read reaches the end of the file.
 *
 * The cache's index lives in memory, and clearing the app's cache from the system settings deletes
 * the files without telling it. Until the process restarts the index still lists every preview, so
 * the files themselves are what is asked.
 *
 * @property cache the preview bytes the player downloaded.
 */
@OptIn(UnstableApi::class)
internal class MediaCachePreviewCache(
    private val cache: Cache,
) : PreviewCache {
    override fun isCached(song: Song): Boolean {
        val contentLength = ContentMetadata.getContentLength(cache.getContentMetadata(song.previewUrl))
        return contentLength != C.LENGTH_UNSET.toLong() &&
            cache.isCached(song.previewUrl, 0, contentLength) &&
            cache.getCachedSpans(song.previewUrl).all { span -> span.file?.exists() != false }
    }
}
