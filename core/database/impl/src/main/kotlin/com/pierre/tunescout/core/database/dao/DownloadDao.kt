package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.DownloadedCollectionEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongExclusionEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * What the user asked to keep on the device. A song is wanted while anything still holds it: its
 * own request, its album's, a playlist's it is in, or the liked songs' while it is liked — unless it
 * was excluded, which taking its own request back does even while a collection still wants it.
 */
@Dao
internal interface DownloadDao {
    @Upsert
    suspend fun upsertSong(entry: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun deleteSong(songId: Long)

    @Upsert
    suspend fun upsertExclusion(entry: DownloadedSongExclusionEntity)

    @Query("DELETE FROM downloaded_song_exclusions WHERE songId = :songId")
    suspend fun deleteExclusion(songId: Long)

    @Upsert
    suspend fun upsertCollection(entry: DownloadedCollectionEntity)

    @Query("DELETE FROM downloaded_collections WHERE kind = :kind AND collectionId = :collectionId")
    suspend fun deleteCollection(
        kind: String,
        collectionId: Long,
    )

    /** The songs of the collection are picked the same way [observeWantedSongs] picks them. */
    @Query(
        """
        DELETE FROM downloaded_songs
        WHERE songId IN (
            SELECT id FROM songs WHERE :kind = 'Album' AND albumId = :collectionId
            UNION
            SELECT songId FROM playlist_songs WHERE :kind = 'Playlist' AND playlistId = :collectionId
            UNION
            SELECT songId FROM favorite_songs WHERE :kind = 'Favorites'
        )
        """,
    )
    suspend fun deleteSongsOfCollection(
        kind: String,
        collectionId: Long,
    )

    /**
     * Taking a collection back also takes back the own request of each of its songs, so none of
     * them stays on the device just because it was once downloaded by itself too. A song another
     * downloaded collection still holds stays, since nothing excludes it.
     */
    @Transaction
    suspend fun deleteCollectionWithItsSongs(
        kind: String,
        collectionId: Long,
    ) {
        deleteSongsOfCollection(kind = kind, collectionId = collectionId)
        deleteCollection(kind = kind, collectionId = collectionId)
    }

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN downloaded_songs ON downloaded_songs.songId = songs.id
        ORDER BY downloaded_songs.requestedAt DESC
        """,
    )
    fun observeOwnSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM downloaded_collections ORDER BY requestedAt ASC")
    fun observeCollections(): Flow<List<DownloadedCollectionEntity>>

    @Query(
        """
        SELECT * FROM songs
        WHERE (
            id IN (SELECT songId FROM downloaded_songs)
            OR albumId IN (SELECT collectionId FROM downloaded_collections WHERE kind = 'Album')
            OR id IN (
                SELECT playlist_songs.songId FROM playlist_songs
                INNER JOIN downloaded_collections
                    ON downloaded_collections.kind = 'Playlist'
                    AND downloaded_collections.collectionId = playlist_songs.playlistId
            )
            OR (
                id IN (SELECT songId FROM favorite_songs)
                AND EXISTS (SELECT 1 FROM downloaded_collections WHERE kind = 'Favorites')
            )
        )
        AND id NOT IN (SELECT songId FROM downloaded_song_exclusions)
        ORDER BY id ASC
        """,
    )
    fun observeWantedSongs(): Flow<List<SongEntity>>

    @Query(
        """
        SELECT EXISTS (
            SELECT 1 FROM songs
            WHERE id = :songId AND (
                id IN (SELECT songId FROM downloaded_songs)
                OR albumId IN (SELECT collectionId FROM downloaded_collections WHERE kind = 'Album')
                OR id IN (
                    SELECT playlist_songs.songId FROM playlist_songs
                    INNER JOIN downloaded_collections
                        ON downloaded_collections.kind = 'Playlist'
                        AND downloaded_collections.collectionId = playlist_songs.playlistId
                )
                OR (
                    id IN (SELECT songId FROM favorite_songs)
                    AND EXISTS (SELECT 1 FROM downloaded_collections WHERE kind = 'Favorites')
                )
            )
            AND id NOT IN (SELECT songId FROM downloaded_song_exclusions)
        )
        """,
    )
    fun observeIsWanted(songId: Long): Flow<Boolean>
}
