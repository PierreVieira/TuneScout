package com.pierre.tunescout

import androidx.test.platform.app.InstrumentationRegistry
import coil3.SingletonImageLoader
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import java.net.InetAddress
import java.util.Base64

/**
 * The bug this guards against: the player draws a size of the cover nothing else in the app
 * requests, so a song downloaded for offline listening could reach the full player screen with
 * that size never fetched and no network left to fetch it. Downloading now warms every size the
 * app draws for that song against the real disk cache, through a real (local) server rather than a
 * mock of the image loader itself.
 */
class DownloadedArtworkCacheTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val imageLoader = SingletonImageLoader.get(context)
    private val server = MockWebServer()

    /** The smallest valid PNG: a single transparent pixel. */
    private val onePixelPng = byteArrayOf(
        0x89.toByte(),
        0x50,
        0x4E,
        0x47,
        0x0D,
        0x0A,
        0x1A,
        0x0A,
        0x00,
        0x00,
        0x00,
        0x0D,
        0x49,
        0x48,
        0x44,
        0x52,
        0x00,
        0x00,
        0x00,
        0x01,
        0x00,
        0x00,
        0x00,
        0x01,
        0x08,
        0x06,
        0x00,
        0x00,
        0x00,
        0x1F,
        0x15,
        0xC4.toByte(),
        0x89.toByte(),
        0x00,
        0x00,
        0x00,
        0x0A,
        0x49,
        0x44,
        0x41,
        0x54,
        0x78,
        0x9C.toByte(),
        0x63,
        0x00,
        0x01,
        0x00,
        0x00,
        0x05,
        0x00,
        0x01,
        0x0D,
        0x0A,
        0x2D,
        0xB4.toByte(),
        0x00,
        0x00,
        0x00,
        0x00,
        0x49,
        0x45,
        0x4E,
        0x44,
        0xAE.toByte(),
        0x42,
        0x60,
        0x82.toByte(),
    )

    private val downloads: DownloadLocalDataSource
        get() = GlobalContext.get().get()

    @BeforeEach
    fun setUp() {
        server.start(InetAddress.getByName("127.0.0.1"), 0)
        repeat(ARTWORK_SIZES_DRAWN) {
            server.enqueue(
                MockResponse
                    .Builder()
                    .addHeader("Content-Type", "image/png")
                    .body(Buffer().write(onePixelPng))
                    .build(),
            )
        }
    }

    @AfterEach
    fun tearDown() {
        server.close()
    }

    @Test
    fun downloadingASongCachesEveryArtworkSizeThePlayerAndTheRestOfTheAppDraw() {
        // Given
        val artwork = Artwork(sourceUrl = "http://127.0.0.1:${server.port}/art/$SONG_ID/100x100bb.jpg")
        val downloadedSong = song(id = SONG_ID, artwork = artwork, previewUrl = dataPreviewUrl())

        // When
        runBlocking { downloads.addSong(downloadedSong) }

        // Then
        assertThat(waitUntilCached(artwork.thumbnailUrl)).isTrue()
        assertThat(waitUntilCached(artwork.mediumUrl)).isTrue()
        assertThat(waitUntilCached(artwork.largeUrl)).isTrue()
    }

    /**
     * A preview the device can fetch with no server behind it, the same trick `DownloadFlowTest` uses.
     *
     * @return a `data:` url carrying its own bytes.
     */
    private fun dataPreviewUrl(): String {
        val bytes = ByteArray(PREVIEW_BYTES) { index -> index.toByte() }
        return "data:audio/mp4;base64,${Base64.getEncoder().encodeToString(bytes)}"
    }

    private fun waitUntilCached(url: String): Boolean {
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            imageLoader.diskCache?.openSnapshot(url)?.use { return true }
            Thread.sleep(POLL_MILLIS)
        }
        return false
    }

    private companion object {
        const val SONG_ID = 9_001L
        const val PREVIEW_BYTES = 2048
        const val ARTWORK_SIZES_DRAWN = 3
        const val TIMEOUT_MILLIS = 15_000L
        const val POLL_MILLIS = 100L
    }
}
