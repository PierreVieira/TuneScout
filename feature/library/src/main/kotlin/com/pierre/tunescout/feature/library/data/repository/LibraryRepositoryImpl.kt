package com.pierre.tunescout.feature.library.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.LibrarySearchLocalDataSource
import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.datastore.write
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val playlistLocalDataSource: PlaylistLocalDataSource,
    private val favoriteSongLocalDataSource: FavoriteSongLocalDataSource,
    private val favoriteAlbumLocalDataSource: FavoriteAlbumLocalDataSource,
    private val librarySearchLocalDataSource: LibrarySearchLocalDataSource,
    private val dataStore: DataStore<Preferences>,
) : LibraryRepository {
    private val viewModeKey = stringPreferencesKey("library_view_mode")

    override fun observePlaylists(): Flow<List<Playlist>> = playlistLocalDataSource.observeAll()

    override fun observePlaylist(playlistId: Long): Flow<Playlist?> = playlistLocalDataSource.observe(playlistId)

    override fun observePlaylistSongs(playlistId: Long): Flow<List<Song>> =
        playlistLocalDataSource.observeSongs(playlistId)

    override fun observeFavorites(): Flow<List<Song>> = favoriteSongLocalDataSource.observeAll()

    override fun observeFavoriteAlbums(): Flow<List<AlbumSummary>> = favoriteAlbumLocalDataSource.observeAll()

    override fun observeViewMode(): Flow<LibraryViewMode> = dataStore.data.map { preferences ->
        preferences[viewModeKey]
            ?.let { stored -> LibraryViewMode.entries.firstOrNull { mode -> mode.name == stored } }
            ?: LibraryViewMode.LIST
    }

    override fun observeRecentSearches(): Flow<List<LibraryItemKey>> = librarySearchLocalDataSource.observeRecent()

    override suspend fun setViewMode(viewMode: LibraryViewMode) {
        dataStore.write(key = viewModeKey, value = viewMode.name)
    }

    override suspend fun createPlaylist(name: String): Long = playlistLocalDataSource.create(name)

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistLocalDataSource.delete(playlistId)
        librarySearchLocalDataSource.remove(LibraryItemKey.Playlist(playlistId))
    }

    override suspend fun addFavorite(song: Song) {
        favoriteSongLocalDataSource.add(song)
    }

    override suspend fun removeFavorite(songId: Long) {
        favoriteSongLocalDataSource.remove(songId)
    }

    override suspend fun recordSearch(key: LibraryItemKey) {
        librarySearchLocalDataSource.record(key)
    }

    override suspend fun removeSearch(key: LibraryItemKey) {
        librarySearchLocalDataSource.remove(key)
    }
}
