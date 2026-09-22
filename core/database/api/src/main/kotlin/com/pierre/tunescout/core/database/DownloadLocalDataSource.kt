package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * What the user asked to keep on the device: songs on their own, and whole collections. Nothing
 * here downloads anything — the player follows [observeWantedSongs] and fetches or drops the files
 * to match it.
 */
interface DownloadLocalDataSource {
    /**
     * @return every song something asks to keep: its own request, its album's, a playlist's it is
     * in, or the liked songs' while it is liked. A song held by two of them is listed once.
     */
    fun observeWantedSongs(): Flow<List<Song>>

    /** @return whether [observeWantedSongs] holds the song with [songId]. */
    fun observeIsWanted(songId: Long): Flow<Boolean>

    /**
     * @return the songs downloaded by their own request, the latest first — whether or not a
     * collection holds them too.
     */
    fun observeOwnSongs(): Flow<List<Song>>

    fun observeCollections(): Flow<Set<LibraryItemKey>>

    /** Saves [song] too, so a song only ever seen in a search is still there to be kept. */
    suspend fun addSong(song: Song)

    /**
     * Takes back the song's own request and keeps it off the device even while an album, a
     * playlist or the liked songs still want it, until it is asked for again.
     */
    suspend fun removeSong(songId: Long)

    /** Does nothing for [LibraryItemKey.DownloadedSongs], which is not a collection to ask for. */
    suspend fun addCollection(key: LibraryItemKey)

    /**
     * Also takes back the own request of each song the collection holds, so undoing a collection's
     * download takes its songs off the device even when one of them was downloaded by itself too.
     * A song another downloaded collection still holds stays.
     */
    suspend fun removeCollection(key: LibraryItemKey)
}
