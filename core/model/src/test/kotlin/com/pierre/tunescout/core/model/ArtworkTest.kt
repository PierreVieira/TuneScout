package com.pierre.tunescout.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

private const val SOURCE_URL = "https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/ab/cd/source/100x100bb.jpg"

class ArtworkTest {
    @ParameterizedTest(name = "GIVEN an iTunes url WHEN reading the {0} url THEN resizes it to {1}")
    @CsvSource(
        "thumbnail, 200x200bb.jpg",
        "medium, 600x600bb.jpg",
        "large, 1000x1000bb.jpg",
    )
    fun `resizes the file name of the source url`(
        variant: String,
        expectedFileName: String,
    ) {
        // Given
        val artwork = Artwork(SOURCE_URL)

        // When
        val url = artwork.urlOf(variant)

        // Then
        assertThat(url).isEqualTo("https://is1-ssl.mzstatic.com/image/thumb/Music124/v4/ab/cd/source/$expectedFileName")
    }

    @Test
    fun `GIVEN a url without a size in the file name WHEN reading a url THEN keeps the source untouched`() {
        // Given
        val artwork = Artwork("https://example.com/art.jpg")

        // When
        val url = artwork.largeUrl

        // Then
        assertThat(url).isEqualTo("https://example.com/art.jpg")
    }

    @Test
    fun `GIVEN a source url with digits in its path WHEN reading a url THEN resizes only the file name`() {
        // Given
        val artwork = Artwork("https://example.com/20x20/60x60bb.jpg")

        // When
        val url = artwork.mediumUrl

        // Then
        assertThat(url).isEqualTo("https://example.com/20x20/600x600bb.jpg")
    }

    @Test
    fun `GIVEN an empty source url WHEN reading a url THEN stays empty`() {
        // Given
        val artwork = Artwork("")

        // When
        val url = artwork.thumbnailUrl

        // Then
        assertThat(url).isEmpty()
    }
}

private fun Artwork.urlOf(variant: String): String = when (variant) {
    "thumbnail" -> thumbnailUrl
    "medium" -> mediumUrl
    else -> largeUrl
}
