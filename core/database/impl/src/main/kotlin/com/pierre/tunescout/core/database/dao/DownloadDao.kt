package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.DownloadedCollectionEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

/**
 * What the user asked to keep on the device. A song is wanted while anything still holds it: its
 * own request, its album's, a playlist's it is in, or the liked songs' while it is liked. Taking one
 * request back leaves it on the device as long as another one is still there.
 */
@Dao
internal interface DownloadDao {
    @Upsert
    suspend fun upsertSong(entry: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun deleteSong(songId: Long)

    @Upsert
    suspend fun upsertCollection(entry: DownloadedCollectionEntity)

    @Query("DELETE FROM downloaded_collections WHERE kind = :kind AND collectionId = :collectionId")
    suspend fun deleteCollection(
        kind: String,
        collectionId: Long,
    )

    @Query("SELECT * FROM downloaded_collections ORDER BY requestedAt ASC")
    fun observeCollections(): Flow<List<DownloadedCollectionEntity>>

    @Query(
        """
        SELECT * FROM songs
        WHERE id IN (SELECT songId FROM downloaded_songs)
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
        )
        """,
    )
    fun observeIsWanted(songId: Long): Flow<Boolean>
}
