package com.pierre.tunescout.feature.library.presentation.mapper

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CollectionStreamsTest {
    @Test
    fun `GIVEN the favourites WHEN observing them THEN the liked songs are the collection`() = runTest {
        // Given
        val useCases = createUseCases(favorites = listOf(song(id = 1)))

        // When
        val songs = CollectionStreams(useCases).observeSongs(CollectionKey.Favorites).first()

        // Then
        assertThat(songs.map(Song::id)).containsExactly(1L)
    }

    @Test
    fun `GIVEN a playlist WHEN observing it THEN its own songs are the collection`() = runTest {
        // Given
        val useCases = createUseCases(favorites = listOf(song(id = 1)), playlistSongs = listOf(song(id = 2)))

        // When
        val songs = CollectionStreams(useCases).observeSongs(CollectionKey.Playlist(playlistId = 7)).first()

        // Then
        assertThat(songs.map(Song::id)).containsExactly(2L)
    }

    @Test
    fun `GIVEN the favourites WHEN observing the title THEN it is the liked songs title`() = runTest {
        // Given
        val useCases = createUseCases()

        // When
        val title = CollectionStreams(useCases).observeTitle(CollectionKey.Favorites).first()

        // Then
        assertThat(title).isEqualTo(CollectionTitle.Favorites)
    }

    @Test
    fun `GIVEN a playlist WHEN observing the title THEN it takes the playlist name`() = runTest {
        // Given
        val useCases = createUseCases(playlist = playlist(id = 7, name = "Road trip"))

        // When
        val title = CollectionStreams(useCases).observeTitle(CollectionKey.Playlist(playlistId = 7)).first()

        // Then
        assertThat(title).isEqualTo(CollectionTitle.Custom(name = "Road trip"))
    }

    @Test
    fun `GIVEN the songs downloaded on their own WHEN observing them THEN they are the collection and its title`() =
        runTest {
            // Given
            val useCases = createUseCases(downloadedSongs = listOf(song(id = 3)))
            val streams = CollectionStreams(useCases)

            // When
            val title = streams.observeTitle(CollectionKey.DownloadedSongs).first()
            val songs = streams.observeSongs(CollectionKey.DownloadedSongs).first()

            // Then
            assertThat(title).isEqualTo(CollectionTitle.DownloadedSongs)
            assertThat(songs.map { song -> song.id }).containsExactly(3L)
        }

    @Test
    fun `GIVEN a playlist that is gone WHEN observing the title THEN there is none`() = runTest {
        // Given
        val useCases = createUseCases(playlist = null)

        // When
        val title = CollectionStreams(useCases).observeTitle(CollectionKey.Playlist(playlistId = 7)).first()

        // Then
        assertThat(title).isNull()
    }

    private fun createUseCases(
        favorites: List<Song> = emptyList(),
        playlistSongs: List<Song> = emptyList(),
        playlist: Playlist? = null,
        downloadedSongs: List<Song> = emptyList(),
    ): CollectionUseCases = CollectionUseCases(
        observePlaylist = { flowOf(playlist) },
        observePlaylistSongs = { flowOf(playlistSongs) },
        observeFavorites = { flowOf(favorites) },
        toggleSongFavorite = { _, _ -> },
        deletePlaylist = { },
        reorderPlaylistSongs = { _, _ -> },
        observeCollectionDownloads = { flowOf(emptySet()) },
        toggleCollectionDownload = { _, _ -> },
        observeDownloadedSongs = { flowOf(downloadedSongs) },
    )
}
