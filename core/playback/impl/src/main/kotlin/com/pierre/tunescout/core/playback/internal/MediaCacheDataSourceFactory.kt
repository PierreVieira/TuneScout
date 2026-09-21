package com.pierre.tunescout.core.playback.internal

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource

/**
 * The data sources a preview is read through: the songs the user downloaded first, then the bytes
 * already played, and the network only for what neither holds. A 30-second preview is small enough
 * that the whole file lands in [cache] the first time it plays, so playing it again works with no
 * connection at all — until the system or the user clears the cache, which never reaches
 * [downloadCache].
 *
 * @property cache the previews played, evicted least-recently-used once full.
 * @property downloadCache the previews the user asked to keep, which only a removed download deletes.
 * @property upstreamFactory what fetches the bytes neither cache holds yet.
 */
@OptIn(UnstableApi::class)
internal class MediaCacheDataSourceFactory(
    private val cache: Cache,
    private val downloadCache: Cache,
    private val upstreamFactory: DataSource.Factory,
) {
    /**
     * @return a factory whose sources read from [downloadCache], then from [cache], and fall back to
     * [upstreamFactory]. The player never writes a download — that is the download manager's job —
     * and a cache read that fails mid-song is reported to the next source instead of to the player,
     * so a corrupted entry only costs the bytes it held.
     */
    fun createDataSourceFactory(): DataSource.Factory = CacheDataSource
        .Factory()
        .setCache(downloadCache)
        .setCacheWriteDataSinkFactory(null)
        .setUpstreamDataSourceFactory(createPlayedCacheFactory(writesPlayedBytes = true))
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    /**
     * @return what the download manager fetches a download through: a song already played is copied
     * from [cache] instead of downloaded again, and nothing it fetches is written there too.
     */
    fun createDownloadUpstreamFactory(): DataSource.Factory = createPlayedCacheFactory(writesPlayedBytes = false)

    private fun createPlayedCacheFactory(writesPlayedBytes: Boolean): DataSource.Factory = CacheDataSource
        .Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .apply { if (!writesPlayedBytes) setCacheWriteDataSinkFactory(null) }
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}
