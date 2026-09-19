package com.pierre.tunescout.feature.library.presentation.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class LibraryItemMatchingTest {
    private val favoritesName = "Liked songs"
    private val playlist = LibraryItemUiModel.Playlist(
        id = 1,
        name = "Road trip",
        songCount = 0,
        artworks = emptyList(),
    )
    private val favorites = LibraryItemUiModel.Favorites(songCount = 0, artworks = emptyList())

    @Test
    fun `GIVEN a playlist WHEN matching part of its name THEN it is a result`() {
        // When
        val isMatching = playlist.isMatching(query = "  ROAD ", favoritesName = favoritesName)

        // Then
        assertThat(isMatching).isTrue()
    }

    @Test
    fun `GIVEN the liked songs row WHEN matching its localized name THEN it is a result`() {
        // When
        val isMatching = favorites.isMatching(query = "liked", favoritesName = favoritesName)

        // Then
        assertThat(isMatching).isTrue()
    }

    @Test
    fun `GIVEN a query that names nothing WHEN matching THEN there is no result`() {
        // When
        val isMatching = playlist.isMatching(query = "podcast", favoritesName = favoritesName)

        // Then
        assertThat(isMatching).isFalse()
    }
}
