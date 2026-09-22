package com.pierre.tunescout.feature.library.presentation.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import org.junit.jupiter.api.Test

class CollectionKeyMapperTest {
    @Test
    fun `GIVEN a collection WHEN naming it as a library item THEN it is the same collection`() {
        // When
        val keys = listOf(
            CollectionKey.Favorites,
            CollectionKey.Playlist(playlistId = 7),
            CollectionKey.DownloadedSongs,
        ).map { key -> key.toLibraryItemKey() }

        // Then
        assertThat(keys)
            .containsExactly(
                LibraryItemKey.Favorites,
                LibraryItemKey.Playlist(playlistId = 7),
                LibraryItemKey.DownloadedSongs,
            ).inOrder()
    }
}
