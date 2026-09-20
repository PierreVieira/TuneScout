package com.pierre.tunescout.core.playback

import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.ContentMetadataMutations
import androidx.media3.datasource.cache.DefaultContentMetadata
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.MediaCachePreviewCache
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class MediaCachePreviewCacheTest {
    private val song = song(id = 1)
    private lateinit var previewCache: MediaCachePreviewCache

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

    private fun prepareScenario(
        contentLength: Long?,
        cachedBytes: Long,
    ) {
        val mutations = ContentMetadataMutations()
        contentLength?.let { length -> ContentMetadataMutations.setContentLength(mutations, length) }
        val cache = mockk<Cache> {
            every { getContentMetadata(song.previewUrl) } returns
                DefaultContentMetadata.EMPTY.copyWithMutationsApplied(mutations)
            every { isCached(song.previewUrl, 0, any()) } answers { thirdArg<Long>() <= cachedBytes }
        }
        previewCache = MediaCachePreviewCache(cache = cache)
    }

    private companion object {
        const val PREVIEW_BYTES = 1_000L
    }
}
