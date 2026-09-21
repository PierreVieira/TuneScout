package com.pierre.tunescout.core.playback

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.MediaCacheDataSourceFactory
import io.mockk.mockk
import org.junit.jupiter.api.Test

class MediaCacheDataSourceFactoryTest {
    private lateinit var factory: MediaCacheDataSourceFactory
    private lateinit var cache: Cache
    private lateinit var downloadCache: Cache

    @Test
    fun `GIVEN both caches WHEN creating the player's data source THEN it reads the downloads first`() {
        // Given
        prepareScenario()

        // When
        val dataSource = factory.createDataSourceFactory().createDataSource()

        // Then
        assertThat(dataSource).isInstanceOf(CacheDataSource::class.java)
        assertThat((dataSource as CacheDataSource).cache).isSameInstanceAs(downloadCache)
    }

    @Test
    fun `GIVEN both caches WHEN creating what a download is fetched through THEN it reads the played previews`() {
        // Given
        prepareScenario()

        // When
        val dataSource = factory.createDownloadUpstreamFactory().createDataSource()

        // Then
        assertThat((dataSource as CacheDataSource).cache).isSameInstanceAs(cache)
    }

    private fun prepareScenario() {
        cache = mockk(relaxed = true)
        downloadCache = mockk(relaxed = true)
        factory = MediaCacheDataSourceFactory(
            cache = cache,
            downloadCache = downloadCache,
            upstreamFactory = DataSource.Factory { mockk(relaxed = true) },
        )
    }
}
