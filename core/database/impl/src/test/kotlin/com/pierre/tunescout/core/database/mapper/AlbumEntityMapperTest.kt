package com.pierre.tunescout.core.database.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import org.junit.jupiter.api.Test

class AlbumEntityMapperTest {
    @Test
    fun `GIVEN songs stored out of order WHEN mapping to album THEN sorts them by track number`() {
        // Given
        val relation = AlbumWithSongs(
            album = album().toEntity(cachedAt = 0),
            songs = listOf(
                song(id = 2, trackNumber = 2).toEntity(),
                song(id = 1, trackNumber = 1).toEntity(),
            ),
        )

        // When
        val album = relation.toAlbum()

        // Then
        assertThat(album.songs.map { song -> song.id }).containsExactly(1L, 2L).inOrder()
    }

    @Test
    fun `GIVEN an album WHEN mapping to entity THEN stores the cache timestamp`() {
        // Given
        val album = album(id = 10)

        // When
        val entity = album.toEntity(cachedAt = 1_000)

        // Then
        assertThat(entity.id).isEqualTo(10L)
        assertThat(entity.cachedAt).isEqualTo(1_000L)
    }
}
