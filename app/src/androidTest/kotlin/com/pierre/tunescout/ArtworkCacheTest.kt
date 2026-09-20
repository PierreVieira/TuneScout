package com.pierre.tunescout

import androidx.test.platform.app.InstrumentationRegistry
import coil3.SingletonImageLoader
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * The artwork on the recently played list has to be there with no connection, which depends on the
 * image loader the app installs rather than on the one Coil would assemble by itself.
 */
class ArtworkCacheTest {
    private val imageLoader = SingletonImageLoader.get(
        InstrumentationRegistry.getInstrumentation().targetContext,
    )

    @Test
    fun theAppKeepsArtworkOnDisk() {
        // When
        val diskCache = imageLoader.diskCache

        // Then
        assertThat(diskCache).isNotNull()
        assertThat(diskCache?.directory?.name).isEqualTo("image_cache")
    }

    @Test
    fun theArtworkCacheNeverShrinksBelowItsFloor() {
        // When
        val maxSize = imageLoader.diskCache?.maxSize

        // Then
        assertThat(maxSize).isAtLeast(64L * 1024 * 1024)
    }
}
