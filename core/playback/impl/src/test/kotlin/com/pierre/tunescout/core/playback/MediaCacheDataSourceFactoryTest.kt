package com.pierre.tunescout.core.playback

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.MediaCacheDataSourceFactory
import io.mockk.mockk
import org.junit.jupiter.api.Test

class MediaCacheDataSourceFactoryTest {
    private lateinit var factory: DataSource.Factory
    private lateinit var cache: Cache

    @Test
    fun `GIVEN a media cache WHEN creating a data source THEN it reads through the cache`() {
        // Given
        prepareScenario()

        // When
        val dataSource = factory.createDataSource()

        // Then
        assertThat(dataSource).isInstanceOf(CacheDataSource::class.java)
    }

    @Test
    fun `GIVEN a media cache WHEN creating a data source THEN it is the one the cache was built on`() {
        // Given
        prepareScenario()

        // When
        val dataSource = factory.createDataSource() as CacheDataSource

        // Then
        assertThat(dataSource.cache).isSameInstanceAs(cache)
    }

    private fun prepareScenario() {
        cache = mockk(relaxed = true)
        factory = MediaCacheDataSourceFactory(
            cache = cache,
            upstreamFactory = DataSource.Factory { mockk(relaxed = true) },
        ).createDataSourceFactory()
    }
}
