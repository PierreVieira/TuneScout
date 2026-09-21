package com.pierre.tunescout.core.playback

import android.net.Uri
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceUtil
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.MediaCacheDataSourceFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MediaCachePlaybackTest {
    private val preview = ByteArray(PREVIEW_BYTES) { index -> index.toByte() }
    private val dataSpec = DataSpec(Uri.parse("https://example.com/preview/1.m4a"))
    private lateinit var cache: SimpleCache
    private lateinit var downloadCache: SimpleCache

    @BeforeEach
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        cache = SimpleCache(
            context.cacheDir.resolve("media_cache_test").apply { deleteRecursively() },
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(context),
        )
        downloadCache = SimpleCache(
            context.cacheDir.resolve("download_cache_test").apply { deleteRecursively() },
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(context),
        )
    }

    @AfterEach
    fun tearDown() {
        cache.release()
        downloadCache.release()
    }

    @Test
    fun aPreviewPlayedOnceIsReadBackWithNoNetworkAtAll() {
        // Given
        readFully(factoryOver(upstream = { ByteArrayDataSource(preview) }))

        // When
        val replayed = readFully(factoryOver(upstream = { OfflineDataSource() }))

        // Then
        assertThat(replayed).isEqualTo(preview)
    }

    @Test
    fun aPreviewPlayedOnceLandsOnDisk() {
        // Given
        val factory = factoryOver(upstream = { ByteArrayDataSource(preview) })

        // When
        readFully(factory)

        // Then
        assertThat(cache.cacheSpace).isEqualTo(preview.size.toLong())
    }

    @Test
    fun aPreviewThatWasNeverPlayedIsFetchedFromTheNetwork() {
        // Given
        var upstreamReads = 0
        val factory = factoryOver(
            upstream = {
                upstreamReads++
                ByteArrayDataSource(preview)
            },
        )

        // When
        val read = readFully(factory)

        // Then
        assertThat(upstreamReads).isEqualTo(1)
        assertThat(read).isEqualTo(preview)
    }

    private fun factoryOver(upstream: () -> DataSource): DataSource.Factory = MediaCacheDataSourceFactory(
        cache = cache,
        downloadCache = downloadCache,
        upstreamFactory = DataSource.Factory { upstream() },
    ).createDataSourceFactory()

    private fun readFully(factory: DataSource.Factory): ByteArray {
        val dataSource = factory.createDataSource()
        dataSource.open(dataSpec)
        return try {
            DataSourceUtil.readToEnd(dataSource)
        } finally {
            dataSource.close()
        }
    }

    private companion object {
        const val PREVIEW_BYTES = 4096
    }
}
