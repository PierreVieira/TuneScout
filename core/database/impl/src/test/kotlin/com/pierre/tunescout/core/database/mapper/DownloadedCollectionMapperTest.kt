package com.pierre.tunescout.core.database.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.entity.DownloadedCollectionEntity
import com.pierre.tunescout.core.model.LibraryItemKey
import org.junit.jupiter.api.Test

internal class DownloadedCollectionMapperTest {
    @Test
    fun `GIVEN a collection WHEN storing and reading it back THEN the same collection comes out`() {
        // Given
        val keys = listOf(
            LibraryItemKey.Favorites,
            LibraryItemKey.Playlist(playlistId = 7),
            LibraryItemKey.Album(albumId = 10),
        )

        // When
        val roundTripped = keys.map { key ->
            key.toDownloadedCollectionEntityOrNull(requestedAt = 1)?.toLibraryItemKeyOrNull()
        }

        // Then
        assertThat(roundTripped).isEqualTo(keys)
    }

    @Test
    fun `GIVEN the liked songs WHEN storing them THEN the row has the kind the queries read and no id`() {
        // When
        val entity = LibraryItemKey.Favorites.toDownloadedCollectionEntityOrNull(requestedAt = 5)

        // Then
        assertThat(entity).isEqualTo(DownloadedCollectionEntity(kind = "Favorites", collectionId = 0, requestedAt = 5))
    }

    @Test
    fun `GIVEN the downloaded songs WHEN storing them THEN there is no row, since each song is asked for on its own`() {
        // When
        val entity = LibraryItemKey.DownloadedSongs.toDownloadedCollectionEntityOrNull(requestedAt = 5)

        // Then
        assertThat(entity).isNull()
    }

    @Test
    fun `GIVEN a kind nothing writes any more WHEN reading it THEN it is dropped`() {
        // Given
        val entity = DownloadedCollectionEntity(kind = "Artist", collectionId = 7, requestedAt = 1)

        // When
        val key = entity.toLibraryItemKeyOrNull()

        // Then
        assertThat(key).isNull()
    }
}
