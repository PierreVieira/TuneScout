package com.pierre.tunescout.core.testing.fake

import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * The liked songs kept in memory, with every add and remove recorded so a test can check them.
 *
 * @param favorites the songs liked when the test starts.
 */
class FakeFavoriteSongLocalDataSource(
    favorites: List<Song> = emptyList(),
) : FavoriteSongLocalDataSource {
    private val favorites = MutableStateFlow(favorites)
    val added = mutableListOf<Song>()
    val removedSongIds = mutableListOf<Long>()

    override fun observeAll(): Flow<List<Song>> = favorites

    override fun observeIsFavorite(songId: Long): Flow<Boolean> =
        favorites.map { songs -> songs.any { song -> song.id == songId } }

    override suspend fun add(song: Song) {
        added += song
        favorites.value += song
    }

    override suspend fun remove(songId: Long) {
        removedSongIds += songId
        favorites.value = favorites.value.filterNot { song -> song.id == songId }
    }
}
