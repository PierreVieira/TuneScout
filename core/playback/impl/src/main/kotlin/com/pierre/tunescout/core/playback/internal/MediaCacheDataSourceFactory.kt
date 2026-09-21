package com.pierre.tunescout.core.playback.internal

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource

/**
 * The data sources ExoPlayer reads a preview through: the bytes already on disk first, the network
 * only for what is missing. A 30-second preview is small enough that the whole file lands in
 * [cache] the first time it plays, so playing it again works with no connection at all.
 *
 * @property cache where the downloaded bytes are kept, evicted least-recently-used once full.
 * @property upstreamFactory what fetches the bytes the cache does not hold yet.
 */
@OptIn(UnstableApi::class)
internal class MediaCacheDataSourceFactory(
    private val cache: Cache,
    private val upstreamFactory: DataSource.Factory,
) {
    /**
     * @return a factory whose sources read from [cache] and fall back to [upstreamFactory]. A
     * cache read that fails mid-song is reported to the upstream instead of to the player, so a
     * corrupted entry only costs the bytes it held.
     */
    fun createDataSourceFactory(): DataSource.Factory = CacheDataSource
        .Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}
