package com.pierre.tunescout.core.playback

import android.net.Uri
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceUtil
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.MediaCacheDataSourceFactory
import com.pierre.tunescout.core.playback.internal.MediaCachePreviewCache
import com.pierre.tunescout.core.testing.fixture.song
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * The two caches the player reads, against real files: the previews played, which the system and
 * the user can clear, and the downloads, which only a removed download deletes.
 */
class SongDownloadCacheTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preview = ByteArray(PREVIEW_BYTES) { index -> index.toByte() }
    private val song = song(id = 1, previewUrl = PREVIEW_URL)
    private val dataSpec = DataSpec(Uri.parse(PREVIEW_URL))
    private val noRequirements = Requirements(0)
    private lateinit var mediaCacheDir: File
    private lateinit var cache: SimpleCache
    private lateinit var downloadCache: SimpleCache
    private lateinit var downloadManager: DownloadManager

    @BeforeEach
    fun setUp() {
        val databaseProvider = StandaloneDatabaseProvider(context)
        mediaCacheDir = context.cacheDir.resolve("media_cache_download_test").apply { deleteRecursively() }
        cache = SimpleCache(mediaCacheDir, LeastRecentlyUsedCacheEvictor(CACHE_BYTES), databaseProvider)
        downloadCache = SimpleCache(
            context.filesDir.resolve("downloads_test").apply { deleteRecursively() },
            NoOpCacheEvictor(),
            databaseProvider,
        )
    }

    @AfterEach
    fun tearDown() {
        instrumentation.runOnMainSync {
            if (::downloadManager.isInitialized) downloadManager.release()
        }
        cache.release()
        downloadCache.release()
    }

    @Test
    fun aDownloadedPreviewIsReadBackWithNoNetworkAtAll() {
        // Given
        download(upstream = { ByteArrayDataSource(preview) })

        // When
        val replayed = readFully(factoryOver(upstream = { OfflineDataSource() }))

        // Then
        assertThat(replayed).isEqualTo(preview)
    }

    @Test
    fun aDownloadedPreviewStillPlaysOfflineAfterThePlayedPreviewsAreCleared() {
        // Given
        readFully(factoryOver(upstream = { ByteArrayDataSource(preview) }))
        download(upstream = { OfflineDataSource() })

        // When
        mediaCacheDir.listFiles().orEmpty().forEach(File::deleteRecursively)
        val replayed = readFully(factoryOver(upstream = { OfflineDataSource() }))

        // Then
        assertThat(replayed).isEqualTo(preview)
    }

    @Test
    fun aPreviewAlreadyPlayedIsDownloadedWithNoNetworkAtAll() {
        // Given
        readFully(factoryOver(upstream = { ByteArrayDataSource(preview) }))

        // When
        val state = download(upstream = { OfflineDataSource() })

        // Then
        assertThat(state).isEqualTo(Download.STATE_COMPLETED)
        assertThat(downloadCache.cacheSpace).isEqualTo(preview.size.toLong())
    }

    @Test
    fun aPlayedPreviewWhoseFilesWereClearedIsNoLongerCountedAsCached() {
        // Given
        readFully(factoryOver(upstream = { ByteArrayDataSource(preview) }))
        val previewCache = MediaCachePreviewCache(cache = cache)
        val cachedBefore = previewCache.isCached(song)

        // When
        mediaCacheDir.listFiles().orEmpty().forEach(File::deleteRecursively)

        // Then
        assertThat(cachedBefore).isTrue()
        assertThat(previewCache.isCached(song)).isFalse()
    }

    /**
     * The manager answers on the thread it was built on, which has to have a looper, so it is built
     * and fed on the main thread, and the test waits for it to settle.
     *
     * @return the state the download ended in.
     */
    private fun download(upstream: () -> DataSource): Int {
        val settled = CountDownLatch(1)
        var finalState = Download.STATE_QUEUED
        instrumentation.runOnMainSync {
            downloadManager = DownloadManager(
                context,
                StandaloneDatabaseProvider(context),
                downloadCache,
                factoryOf(upstream).createDownloadUpstreamFactory(),
                Executors.newSingleThreadExecutor(),
            )
            downloadManager.addListener(
                object : DownloadManager.Listener {
                    override fun onDownloadChanged(
                        downloadManager: DownloadManager,
                        download: Download,
                        finalException: Exception?,
                    ) {
                        if (download.isTerminalState) {
                            finalState = download.state
                            settled.countDown()
                        }
                    }
                },
            )
            downloadManager.requirements = noRequirements
            downloadManager.resumeDownloads()
            downloadManager.addDownload(DownloadRequest.Builder(song.id.toString(), Uri.parse(PREVIEW_URL)).build())
        }
        assertThat(settled.await(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        return finalState
    }

    private fun factoryOf(upstream: () -> DataSource): MediaCacheDataSourceFactory = MediaCacheDataSourceFactory(
        cache = cache,
        downloadCache = downloadCache,
        upstreamFactory = DataSource.Factory { upstream() },
    )

    private fun factoryOver(upstream: () -> DataSource): DataSource.Factory =
        factoryOf(upstream).createDataSourceFactory()

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
        const val CACHE_BYTES = 1024L * 1024
        const val PREVIEW_URL = "https://example.com/preview/1.m4a"
        const val DOWNLOAD_TIMEOUT_SECONDS = 10L
    }
}
