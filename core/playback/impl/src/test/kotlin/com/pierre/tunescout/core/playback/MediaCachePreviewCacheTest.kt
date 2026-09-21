package com.pierre.tunescout.core.playback

import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.ContentMetadataMutations
import androidx.media3.datasource.cache.DefaultContentMetadata
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.MediaCachePreviewCache
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MediaCachePreviewCacheTest {
    private val song = song(id = 1)
    private lateinit var previewCache: MediaCachePreviewCache

    @TempDir
    lateinit var cacheDir: File

    @Test
    fun `GIVEN the whole preview on disk WHEN asking whether it is cached THEN it is`() {
        // Given
        prepareScenario(contentLength = PREVIEW_BYTES, cachedBytes = PREVIEW_BYTES)

        // When
        val isCached = previewCache.isCached(song)

        // Then
        assertThat(isCached).isTrue()
    }

    @Test
    fun `GIVEN only part of the preview on disk WHEN asking whether it is cached THEN it is not`() {
        // Given
        prepareScenario(contentLength = PREVIEW_BYTES, cachedBytes = PREVIEW_BYTES / 2)

        // When
        val isCached = previewCache.isCached(song)

        // Then
        assertThat(isCached).isFalse()
    }

    @Test
    fun `GIVEN a preview that was never played WHEN asking whether it is cached THEN it is not`() {
        // Given
        prepareScenario(contentLength = null, cachedBytes = 0)

        // When
        val isCached = previewCache.isCached(song)

        // Then
        assertThat(isCached).isFalse()
    }

    @Test
    fun `GIVEN the preview indexed but its file cleared WHEN asking whether it is cached THEN it is not`() {
        // Given
        prepareScenario(contentLength = PREVIEW_BYTES, cachedBytes = PREVIEW_BYTES, isFileOnDisk = false)

        // When
        val isCached = previewCache.isCached(song)

        // Then
        assertThat(isCached).isFalse()
    }

    private fun prepareScenario(
        contentLength: Long?,
        cachedBytes: Long,
        isFileOnDisk: Boolean = true,
    ) {
        val file = File(cacheDir, "0.0.1.v3.exo").apply { if (isFileOnDisk) writeBytes(ByteArray(1)) }
        val span = CacheSpan(song.previewUrl, 0, cachedBytes, 1, file)
        val mutations = ContentMetadataMutations()
        contentLength?.let { length -> ContentMetadataMutations.setContentLength(mutations, length) }
        val cache = mockk<Cache> {
            every { getContentMetadata(song.previewUrl) } returns
                DefaultContentMetadata.EMPTY.copyWithMutationsApplied(mutations)
            every { isCached(song.previewUrl, 0, any()) } answers { thirdArg<Long>() <= cachedBytes }
            every { getCachedSpans(song.previewUrl) } returns sortedSetOf(span)
        }
        previewCache = MediaCachePreviewCache(cache = cache)
    }

    private companion object {
        const val PREVIEW_BYTES = 1_000L
    }
}
