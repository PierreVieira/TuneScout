package com.pierre.tunescout.core.database.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.relation.PlaylistRow
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Playlist
import org.junit.jupiter.api.Test

internal class PlaylistRowMapperTest {
    @Test
    fun `GIVEN rows of two playlists WHEN mapping THEN groups them keeping the query order`() {
        // Given
        val rows = listOf(
            row(playlistId = 2, name = "Road trip", artworkUrl = "a"),
            row(playlistId = 2, name = "Road trip", artworkUrl = "b"),
            row(playlistId = 1, name = "Focus", artworkUrl = "c"),
        )

        // When
        val playlists = rows.toPlaylists()

        // Then
        assertThat(playlists.map(Playlist::id)).containsExactly(2L, 1L).inOrder()
        assertThat(playlists.first().artworks).containsExactly(Artwork("a"), Artwork("b")).inOrder()
    }

    @Test
    fun `GIVEN a playlist with no songs WHEN mapping THEN counts zero and carries no artwork`() {
        // Given
        val rows = listOf(row(playlistId = 1, name = "Focus", artworkUrl = null))

        // When
        val playlist = rows.toPlaylists().single()

        // Then
        assertThat(playlist.songCount).isEqualTo(0)
        assertThat(playlist.artworks).isEmpty()
    }

    private fun row(
        playlistId: Long,
        name: String,
        artworkUrl: String?,
    ): PlaylistRow = PlaylistRow(playlistId = playlistId, name = name, artworkUrl = artworkUrl)
}
