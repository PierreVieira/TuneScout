package com.pierre.tunescout.feature.library.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.albumSummary
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.impl.CreatePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.DeletePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveFavoriteAlbumsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveFavoritesUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveLibraryViewModeUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistSongsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveRecentLibrarySearchesUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RecordLibrarySearchUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RemoveLibrarySearchUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ReorderPlaylistSongsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.SetLibraryViewModeUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ToggleSongFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LibraryUseCasesTest {
    private lateinit var repository: FakeLibraryRepository

    @Test
    fun `GIVEN playlists in the library WHEN observing them THEN they come back`() = runTest {
        // Given
        val playlists = listOf(playlist(id = 1), playlist(id = 2, name = "Focus"))
        prepareScenario(playlists = playlists)

        // When
        val observed = ObservePlaylistsUseCase(repository)()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(playlists).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN a playlist WHEN observing it THEN the one asked for comes back`() = runTest {
        // Given
        prepareScenario(playlists = listOf(playlist(id = 7, name = "Focus")))

        // When
        val observed = ObservePlaylistUseCase(repository)(playlistId = 7)

        // Then
        observed.test {
            assertThat(awaitItem()?.name).isEqualTo("Focus")
            awaitComplete()
        }
        assertThat(repository.observedPlaylistIds).containsExactly(7L)
    }

    @Test
    fun `GIVEN a playlist with songs WHEN observing them THEN they come back in order`() = runTest {
        // Given
        val songs = listOf(song(id = 1), song(id = 2))
        prepareScenario(playlistSongs = songs)

        // When
        val observed = ObservePlaylistSongsUseCase(repository)(playlistId = 7)

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(songs).inOrder()
            awaitComplete()
        }
        assertThat(repository.observedPlaylistSongsIds).containsExactly(7L)
    }

    @Test
    fun `GIVEN liked songs WHEN observing the favourites THEN they come back`() = runTest {
        // Given
        val favorites = listOf(song(id = 3))
        prepareScenario(favorites = favorites)

        // When
        val observed = ObserveFavoritesUseCase(repository)()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(favorites)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN liked albums WHEN observing them THEN they come back`() = runTest {
        // Given
        val albums = listOf(albumSummary(id = 4))
        prepareScenario(favoriteAlbums = albums)

        // When
        val observed = ObserveFavoriteAlbumsUseCase(repository)()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(albums)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN a chosen view mode WHEN observing it THEN it comes back`() = runTest {
        // Given
        prepareScenario(viewMode = LibraryViewMode.GRID)

        // When
        val observed = ObserveLibraryViewModeUseCase(repository)()

        // Then
        observed.test {
            assertThat(awaitItem()).isEqualTo(LibraryViewMode.GRID)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN recent searches WHEN observing them THEN they come back in order`() = runTest {
        // Given
        val keys = listOf(LibraryItemKey.Favorites, LibraryItemKey.Playlist(playlistId = 7))
        prepareScenario(recentSearches = keys)

        // When
        val observed = ObserveRecentLibrarySearchesUseCase(repository)()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(keys).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `WHEN creating a playlist THEN its new id comes back`() = runTest {
        // Given
        prepareScenario(createdPlaylistId = 42)

        // When
        val playlistId = CreatePlaylistUseCase(repository)(name = "Road trip")

        // Then
        assertThat(playlistId).isEqualTo(42)
        assertThat(repository.createdNames).containsExactly("Road trip")
    }

    @Test
    fun `WHEN deleting a playlist THEN the library drops it`() = runTest {
        // Given
        prepareScenario()

        // When
        DeletePlaylistUseCase(repository)(playlistId = 7)

        // Then
        assertThat(repository.deletedPlaylistIds).containsExactly(7L)
    }

    @Test
    fun `WHEN reordering a playlist THEN hands the new order to the repository`() = runTest {
        // Given
        prepareScenario()

        // When
        ReorderPlaylistSongsUseCase(repository)(playlistId = 7, songIds = listOf(2, 1))

        // Then
        assertThat(repository.reorders).containsExactly(7L to listOf(2L, 1L))
    }

    @Test
    fun `GIVEN a song that is not liked WHEN toggling it THEN it joins the favourites`() = runTest {
        // Given
        prepareScenario()

        // When
        ToggleSongFavoriteUseCase(repository)(song = song(id = 1), isFavorite = false)

        // Then
        assertThat(repository.addedFavorites).containsExactly(song(id = 1))
        assertThat(repository.removedFavoriteIds).isEmpty()
    }

    @Test
    fun `GIVEN a liked song WHEN toggling it THEN it leaves the favourites`() = runTest {
        // Given
        prepareScenario()

        // When
        ToggleSongFavoriteUseCase(repository)(song = song(id = 1), isFavorite = true)

        // Then
        assertThat(repository.removedFavoriteIds).containsExactly(1L)
        assertThat(repository.addedFavorites).isEmpty()
    }

    @Test
    fun `WHEN choosing a view mode THEN it is stored`() = runTest {
        // Given
        prepareScenario()

        // When
        SetLibraryViewModeUseCase(repository)(LibraryViewMode.GRID)

        // Then
        assertThat(repository.storedViewModes).containsExactly(LibraryViewMode.GRID)
    }

    @Test
    fun `WHEN opening an item from the search THEN it becomes a recent search`() = runTest {
        // Given
        prepareScenario()

        // When
        RecordLibrarySearchUseCase(repository)(LibraryItemKey.Favorites)

        // Then
        assertThat(repository.recordedSearches).containsExactly(LibraryItemKey.Favorites)
    }

    @Test
    fun `WHEN dismissing a recent search THEN it is removed`() = runTest {
        // Given
        prepareScenario()

        // When
        RemoveLibrarySearchUseCase(repository)(LibraryItemKey.Album(albumId = 4))

        // Then
        assertThat(repository.removedSearches).containsExactly(LibraryItemKey.Album(albumId = 4))
    }

    private fun prepareScenario(
        playlists: List<Playlist> = emptyList(),
        playlistSongs: List<Song> = emptyList(),
        favorites: List<Song> = emptyList(),
        favoriteAlbums: List<AlbumSummary> = emptyList(),
        recentSearches: List<LibraryItemKey> = emptyList(),
        viewMode: LibraryViewMode = LibraryViewMode.LIST,
        createdPlaylistId: Long = 1,
    ) {
        repository = FakeLibraryRepository(
            playlists = playlists,
            playlistSongs = playlistSongs,
            favorites = favorites,
            favoriteAlbums = favoriteAlbums,
            recentSearches = recentSearches,
            viewMode = viewMode,
            createdPlaylistId = createdPlaylistId,
        )
    }
}

private class FakeLibraryRepository(
    private val playlists: List<Playlist>,
    private val playlistSongs: List<Song>,
    private val favorites: List<Song>,
    private val favoriteAlbums: List<AlbumSummary>,
    private val recentSearches: List<LibraryItemKey>,
    private val viewMode: LibraryViewMode,
    private val createdPlaylistId: Long,
) : LibraryRepository {
    val observedPlaylistIds = mutableListOf<Long>()
    val observedPlaylistSongsIds = mutableListOf<Long>()
    val createdNames = mutableListOf<String>()
    val deletedPlaylistIds = mutableListOf<Long>()
    val reorders = mutableListOf<Pair<Long, List<Long>>>()
    val addedFavorites = mutableListOf<Song>()
    val removedFavoriteIds = mutableListOf<Long>()
    val storedViewModes = mutableListOf<LibraryViewMode>()
    val recordedSearches = mutableListOf<LibraryItemKey>()
    val removedSearches = mutableListOf<LibraryItemKey>()

    override fun observePlaylists(): Flow<List<Playlist>> = flowOf(playlists)

    override fun observePlaylist(playlistId: Long): Flow<Playlist?> {
        observedPlaylistIds += playlistId
        return flowOf(playlists.firstOrNull { playlist -> playlist.id == playlistId })
    }

    override fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> {
        observedPlaylistSongsIds += playlistId
        return flowOf(playlistSongs)
    }

    override fun observeFavorites(): Flow<List<Song>> = flowOf(favorites)

    override fun observeFavoriteAlbums(): Flow<List<AlbumSummary>> = flowOf(favoriteAlbums)

    override fun observeViewMode(): Flow<LibraryViewMode> = flowOf(viewMode)

    override fun observeRecentSearches(): Flow<List<LibraryItemKey>> = flowOf(recentSearches)

    override suspend fun setViewMode(viewMode: LibraryViewMode) {
        storedViewModes += viewMode
    }

    override suspend fun createPlaylist(name: String): Long {
        createdNames += name
        return createdPlaylistId
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        deletedPlaylistIds += playlistId
    }

    override suspend fun reorderPlaylistSongs(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        reorders += playlistId to songIds
    }

    override suspend fun addFavorite(song: Song) {
        addedFavorites += song
    }

    override suspend fun removeFavorite(songId: Long) {
        removedFavoriteIds += songId
    }

    override suspend fun recordSearch(key: LibraryItemKey) {
        recordedSearches += key
    }

    override suspend fun removeSearch(key: LibraryItemKey) {
        removedSearches += key
    }
}
