package com.pierre.tunescout.core.testing.fake

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * The download requests kept in memory. What a collection holds is not worked out here: a test
 * says which songs are wanted through [wanted], and which of those a collection keeps through
 * [heldByCollectionIds], so taking a song's own request back leaves those wanted.
 *
 * @property heldByCollectionIds the songs a collection keeps wanted whatever their own request.
 * @param wanted the songs wanted when the test starts.
 * @param collections the collections asked for when the test starts.
 * @param ownSongs the songs downloaded by their own request when the test starts, the latest first.
 */
class FakeDownloadLocalDataSource(
    val heldByCollectionIds: Set<Long> = emptySet(),
    wanted: List<Song> = emptyList(),
    collections: Set<LibraryItemKey> = emptySet(),
    ownSongs: List<Song> = emptyList(),
) : DownloadLocalDataSource {
    val wanted = MutableStateFlow(wanted)
    val collections = MutableStateFlow(collections)
    val ownSongs = MutableStateFlow(ownSongs)
    val addedSongs = mutableListOf<Song>()
    val removedSongIds = mutableListOf<Long>()

    override fun observeWantedSongs(): Flow<List<Song>> = wanted

    override fun observeIsWanted(songId: Long): Flow<Boolean> =
        wanted.map { songs -> songs.any { song -> song.id == songId } }

    override fun observeOwnSongs(): Flow<List<Song>> = ownSongs

    override fun observeCollections(): Flow<Set<LibraryItemKey>> = collections

    override suspend fun addSong(song: Song) {
        addedSongs += song
        ownSongs.value = listOf(song) + ownSongs.value.filterNot { current -> current.id == song.id }
        if (wanted.value.none { current -> current.id == song.id }) wanted.value += song
    }

    override suspend fun removeSong(songId: Long) {
        removedSongIds += songId
        ownSongs.value = ownSongs.value.filterNot { song -> song.id == songId }
        if (songId !in heldByCollectionIds) wanted.value = wanted.value.filterNot { song -> song.id == songId }
    }

    override suspend fun addCollection(key: LibraryItemKey) {
        if (key == LibraryItemKey.DownloadedSongs) return
        collections.value += key
    }

    override suspend fun removeCollection(key: LibraryItemKey) {
        collections.value -= key
    }
}
