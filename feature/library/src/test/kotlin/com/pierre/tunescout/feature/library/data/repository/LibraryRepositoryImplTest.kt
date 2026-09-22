package com.pierre.tunescout.feature.library.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.database.LibrarySearchLocalDataSource
import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fake.FakeFavoriteSongLocalDataSource
import com.pierre.tunescout.core.testing.fixture.albumSummary
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LibraryRepositoryImplTest {
    private lateinit var repository: LibraryRepositoryImpl
    private lateinit var playlistLocalDataSource: FakePlaylistLocalDataSource
    private lateinit var favoriteSongLocalDataSource: FakeFavoriteSongLocalDataSource
    private lateinit var favoriteAlbumLocalDataSource: FakeFavoriteAlbumLocalDataSource
    private lateinit var librarySearchLocalDataSource: FakeLibrarySearchLocalDataSource

    @Test
    fun `GIVEN stored playlists WHEN observing them THEN they come back as they are`() = runTest {
        // Given
        val playlists = listOf(playlist(id = 1), playlist(id = 2, name = "Focus"))
        prepareScenario(playlists = playlists)

        // When
        val observed = repository.observePlaylists()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(playlists).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN a stored playlist WHEN observing it by id THEN that one comes back`() = runTest {
        // Given
        prepareScenario(playlists = listOf(playlist(id = 7, name = "Focus")))

        // When
        val observed = repository.observePlaylist(playlistId = 7)

        // Then
        observed.test {
            assertThat(awaitItem()?.name).isEqualTo("Focus")
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN a playlist with songs WHEN observing them THEN they come back in order`() = runTest {
        // Given
        val songs = listOf(song(id = 1), song(id = 2))
        prepareScenario(playlistSongs = songs)

        // When
        val observed = repository.observePlaylistSongs(playlistId = 7)

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(songs).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN liked songs WHEN observing the favourites THEN they come back`() = runTest {
        // Given
        val favorites = listOf(song(id = 3))
        prepareScenario(favorites = favorites)

        // When
        val observed = repository.observeFavorites()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(favorites)
        }
    }

    @Test
    fun `GIVEN liked albums WHEN observing them THEN they come back`() = runTest {
        // Given
        val albums = listOf(albumSummary(id = 4))
        prepareScenario(favoriteAlbums = albums)

        // When
        val observed = repository.observeFavoriteAlbums()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(albums)
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN recent searches WHEN observing them THEN they come back`() = runTest {
        // Given
        val keys = listOf(LibraryItemKey.Favorites, LibraryItemKey.Playlist(playlistId = 7))
        prepareScenario(recentSearches = keys)

        // When
        val observed = repository.observeRecentSearches()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(keys).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN an empty store WHEN observing the view mode THEN the library opens as a list`() = runTest {
        // Given
        prepareScenario()

        // When
        val viewMode = repository.observeViewMode().first()

        // Then
        assertThat(viewMode).isEqualTo(LibraryViewMode.LIST)
    }

    @Test
    fun `WHEN choosing the grid THEN the observed view mode follows`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setViewMode(LibraryViewMode.GRID)

        // Then
        assertThat(repository.observeViewMode().first()).isEqualTo(LibraryViewMode.GRID)
    }

    @Test
    fun `GIVEN a stored view mode this version no longer has WHEN observing THEN it falls back to a list`() = runTest {
        // Given
        prepareScenario(storedViewMode = "CAROUSEL")

        // When
        val viewMode = repository.observeViewMode().first()

        // Then
        assertThat(viewMode).isEqualTo(LibraryViewMode.LIST)
    }

    @Test
    fun `GIVEN an empty store WHEN observing the grid size THEN two share a row`() = runTest {
        // Given
        prepareScenario()

        // When
        val columns = repository.observeGridColumns().first()

        // Then
        assertThat(columns).isEqualTo(LibraryGridColumns.TWO)
    }

    @Test
    fun `WHEN choosing three per row THEN the observed grid size follows`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.setGridColumns(LibraryGridColumns.THREE)

        // Then
        assertThat(repository.observeGridColumns().first()).isEqualTo(LibraryGridColumns.THREE)
    }

    @Test
    fun `GIVEN a stored grid size this version does not offer WHEN observing THEN it falls back to two`() = runTest {
        // Given
        prepareScenario(storedGridColumns = 5)

        // When
        val columns = repository.observeGridColumns().first()

        // Then
        assertThat(columns).isEqualTo(LibraryGridColumns.TWO)
    }

    @Test
    fun `WHEN creating a playlist THEN its new id comes back`() = runTest {
        // Given
        prepareScenario(createdPlaylistId = 42)

        // When
        val playlistId = repository.createPlaylist(name = "Road trip")

        // Then
        assertThat(playlistId).isEqualTo(42)
        assertThat(playlistLocalDataSource.createdNames).containsExactly("Road trip")
    }

    @Test
    fun `WHEN deleting a playlist THEN it also stops being a recent search`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.deletePlaylist(playlistId = 7)

        // Then
        assertThat(playlistLocalDataSource.deletedPlaylistIds).containsExactly(7L)
        assertThat(librarySearchLocalDataSource.removed).containsExactly(LibraryItemKey.Playlist(playlistId = 7))
    }

    @Test
    fun `WHEN reordering a playlist THEN hands the new order to the playlist store`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.reorderPlaylistSongs(playlistId = 7, songIds = listOf(3, 1, 2))

        // Then
        assertThat(playlistLocalDataSource.reorders).containsExactly(7L to listOf(3L, 1L, 2L))
    }

    @Test
    fun `WHEN liking a song THEN it joins the favourites`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.addFavorite(song(id = 1))

        // Then
        assertThat(favoriteSongLocalDataSource.added).containsExactly(song(id = 1))
    }

    @Test
    fun `WHEN unliking a song THEN it leaves the favourites`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.removeFavorite(songId = 1)

        // Then
        assertThat(favoriteSongLocalDataSource.removedSongIds).containsExactly(1L)
    }

    @Test
    fun `WHEN opening an item from the search THEN it is recorded as a recent one`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.recordSearch(LibraryItemKey.Favorites)

        // Then
        assertThat(librarySearchLocalDataSource.recorded).containsExactly(LibraryItemKey.Favorites)
    }

    @Test
    fun `WHEN dismissing a recent search THEN it is removed`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.removeSearch(LibraryItemKey.Album(albumId = 4))

        // Then
        assertThat(librarySearchLocalDataSource.removed).containsExactly(LibraryItemKey.Album(albumId = 4))
    }

    private fun prepareScenario(
        playlists: List<Playlist> = emptyList(),
        playlistSongs: List<Song> = emptyList(),
        favorites: List<Song> = emptyList(),
        favoriteAlbums: List<AlbumSummary> = emptyList(),
        recentSearches: List<LibraryItemKey> = emptyList(),
        storedViewMode: String? = null,
        storedGridColumns: Int? = null,
        createdPlaylistId: Long = 1,
    ) {
        playlistLocalDataSource = FakePlaylistLocalDataSource(
            playlists = playlists,
            playlistSongs = playlistSongs,
            createdPlaylistId = createdPlaylistId,
        )
        favoriteSongLocalDataSource = FakeFavoriteSongLocalDataSource(favorites = favorites)
        favoriteAlbumLocalDataSource = FakeFavoriteAlbumLocalDataSource(favoriteAlbums = favoriteAlbums)
        librarySearchLocalDataSource = FakeLibrarySearchLocalDataSource(recentSearches = recentSearches)
        repository = LibraryRepositoryImpl(
            playlistLocalDataSource = playlistLocalDataSource,
            favoriteSongLocalDataSource = favoriteSongLocalDataSource,
            favoriteAlbumLocalDataSource = favoriteAlbumLocalDataSource,
            librarySearchLocalDataSource = librarySearchLocalDataSource,
            dataStore = FakePreferencesDataStore(
                storedViewMode = storedViewMode,
                storedGridColumns = storedGridColumns,
            ),
        )
    }
}

private class FakePreferencesDataStore(
    storedViewMode: String?,
    storedGridColumns: Int?,
) : DataStore<Preferences> {
    override val data: Flow<Preferences>
        field = MutableStateFlow<Preferences>(
            mutablePreferencesOf().apply {
                storedViewMode?.let { stored -> this[stringPreferencesKey("library_view_mode")] = stored }
                storedGridColumns?.let { stored -> this[intPreferencesKey("library_grid_columns")] = stored }
            },
        )

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(data.value).also { updated -> data.value = updated }
}

private class FakePlaylistLocalDataSource(
    private val playlists: List<Playlist>,
    private val playlistSongs: List<Song>,
    private val createdPlaylistId: Long,
) : PlaylistLocalDataSource {
    val createdNames = mutableListOf<String>()
    val deletedPlaylistIds = mutableListOf<Long>()
    val reorders = mutableListOf<Pair<Long, List<Long>>>()

    override fun observeAll(): Flow<List<Playlist>> = flowOf(playlists)

    override fun observe(playlistId: Long): Flow<Playlist?> = flowOf(
        playlists.firstOrNull { playlist -> playlist.id == playlistId },
    )

    override fun observeSongs(playlistId: Long): Flow<List<Song>> = flowOf(playlistSongs)

    override fun observeContains(
        playlistId: Long,
        songId: Long,
    ): Flow<Boolean> = error("unused")

    override suspend fun create(name: String): Long {
        createdNames += name
        return createdPlaylistId
    }

    override suspend fun rename(
        playlistId: Long,
        name: String,
    ) {
        error("unused")
    }

    override suspend fun delete(playlistId: Long) {
        deletedPlaylistIds += playlistId
    }

    override suspend fun addSong(
        playlistId: Long,
        song: Song,
    ) {
        error("unused")
    }

    override suspend fun removeSong(
        playlistId: Long,
        songId: Long,
    ) {
        error("unused")
    }

    override suspend fun reorderSongs(
        playlistId: Long,
        songIds: List<Long>,
    ) {
        reorders += playlistId to songIds
    }
}

private class FakeFavoriteAlbumLocalDataSource(
    private val favoriteAlbums: List<AlbumSummary>,
) : FavoriteAlbumLocalDataSource {
    override fun observeAll(): Flow<List<AlbumSummary>> = flowOf(favoriteAlbums)

    override fun observeIsFavorite(albumId: Long): Flow<Boolean> = error("unused")

    override suspend fun add(album: Album) {
        error("unused")
    }

    override suspend fun remove(albumId: Long) {
        error("unused")
    }
}

private class FakeLibrarySearchLocalDataSource(
    private val recentSearches: List<LibraryItemKey>,
) : LibrarySearchLocalDataSource {
    val recorded = mutableListOf<LibraryItemKey>()
    val removed = mutableListOf<LibraryItemKey>()

    override fun observeRecent(): Flow<List<LibraryItemKey>> = flowOf(recentSearches)

    override suspend fun record(key: LibraryItemKey) {
        recorded += key
    }

    override suspend fun remove(key: LibraryItemKey) {
        removed += key
    }
}
