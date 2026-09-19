package com.pierre.tunescout.feature.library.presentation.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import org.junit.jupiter.api.Test

class LibraryItemUiModelMapperTest {
    @Test
    fun `GIVEN more liked songs than a cover holds WHEN building the row THEN keeps the count and four covers`() {
        // Given
        val favorites = (1L..6L).map { id -> song(id = id) }

        // When
        val item = favorites.toFavoritesItem()

        // Then
        assertThat(item.songCount).isEqualTo(6)
        assertThat(item.artworks).hasSize(4)
    }

    @Test
    fun `GIVEN a playlist WHEN mapping it THEN caps its cover at four artworks`() {
        // Given
        val artworks = (1..6).map { index -> Artwork("https://example.com/$index/100x100bb.jpg") }

        // When
        val item = playlist(id = 7, name = "Road trip", songCount = 6, artworks = artworks).toUiModel()

        // Then
        assertThat(item).isEqualTo(
            LibraryItemUiModel.Playlist(
                id = 7,
                name = "Road trip",
                songCount = 6,
                artworks = artworks.take(4),
            ),
        )
    }

    @Test
    fun `GIVEN liked songs and playlists WHEN building the library THEN the liked songs come first`() {
        // When
        val items = buildLibraryItems(favorites = listOf(song()), playlists = listOf(playlist(id = 7)))

        // Then
        assertThat(items.first()).isInstanceOf(LibraryItemUiModel.Favorites::class.java)
        assertThat(items.last()).isInstanceOf(LibraryItemUiModel.Playlist::class.java)
    }
}
