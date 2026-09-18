package com.pierre.tunescout.core.network.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.network.dto.ResultDto
import org.junit.jupiter.api.Test

class AlbumMapperTest {
    @Test
    fun `GIVEN a collection and its tracks WHEN mapping THEN builds the album with songs by track number`() {
        // Given
        val results = listOf(
            collectionResult(),
            songResult().copy(trackId = 2L, trackNumber = 2),
            songResult().copy(trackId = 1L, trackNumber = 1),
        )

        // When
        val album = results.toAlbumOrNull()

        // Then
        assertThat(album?.id).isEqualTo(10L)
        assertThat(album?.title).isEqualTo("Random Access Memories")
        assertThat(album?.artistName).isEqualTo("Daft Punk")
        assertThat(album?.songs?.map { song -> song.id }).containsExactly(1L, 2L).inOrder()
    }

    @Test
    fun `GIVEN results without a collection WHEN mapping THEN returns null`() {
        // Given
        val results = listOf(songResult())

        // When
        val album = results.toAlbumOrNull()

        // Then
        assertThat(album).isNull()
    }

    @Test
    fun `GIVEN an empty lookup WHEN mapping THEN returns null`() {
        // Given
        val results = emptyList<ResultDto>()

        // When
        val album = results.toAlbumOrNull()

        // Then
        assertThat(album).isNull()
    }
}

internal fun collectionResult(): ResultDto = ResultDto(
    wrapperType = "collection",
    collectionId = 10L,
    collectionName = "Random Access Memories",
    artistName = "Daft Punk",
    artworkUrl100 = "https://example.com/album.jpg",
)
