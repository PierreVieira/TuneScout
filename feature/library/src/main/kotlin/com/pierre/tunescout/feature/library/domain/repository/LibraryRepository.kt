package com.pierre.tunescout.feature.library.domain.repository

import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun observePlaylists(): Flow<List<Playlist>>

    fun observePlaylist(playlistId: Long): Flow<Playlist?>

    fun observePlaylistSongs(playlistId: Long): Flow<List<Song>>

    fun observeFavorites(): Flow<List<Song>>

    fun observeFavoriteAlbums(): Flow<List<AlbumSummary>>

    fun observeViewMode(): Flow<LibraryViewMode>

    fun observeRecentSearches(): Flow<List<LibraryItemKey>>

    suspend fun setViewMode(viewMode: LibraryViewMode)

    suspend fun createPlaylist(name: String): Long

    suspend fun deletePlaylist(playlistId: Long)

    suspend fun removeSongFromPlaylist(
        playlistId: Long,
        songId: Long,
    )

    suspend fun removeFavorite(songId: Long)

    suspend fun recordSearch(key: LibraryItemKey)

    suspend fun removeSearch(key: LibraryItemKey)
}
