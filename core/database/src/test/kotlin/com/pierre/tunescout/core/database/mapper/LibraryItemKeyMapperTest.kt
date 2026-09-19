package com.pierre.tunescout.core.database.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.LibraryItemKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class LibraryItemKeyMapperTest {
    @Test
    fun `GIVEN a key WHEN storing and reading it back THEN the same key comes out`() {
        // Given
        val keys = listOf(
            LibraryItemKey.Favorites,
            LibraryItemKey.Playlist(playlistId = 7),
            LibraryItemKey.Album(albumId = 10),
        )

        // When
        val roundTripped = keys.map { key -> key.toItemId().toLibraryItemKeyOrNull() }

        // Then
        assertThat(roundTripped).isEqualTo(keys)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "playlist:", "playlist:abc", "album:", "album:abc", "artist:7", "favourites"])
    fun `GIVEN a stored id nothing writes any more WHEN reading it THEN it is dropped`(itemId: String) {
        // When
        val key = itemId.toLibraryItemKeyOrNull()

        // Then
        assertThat(key).isNull()
    }
}
