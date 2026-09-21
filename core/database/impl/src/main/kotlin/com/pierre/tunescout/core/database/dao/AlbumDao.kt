package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.AlbumTrackOrderEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AlbumDao {
    @Upsert
    suspend fun upsert(album: AlbumEntity)

    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun observeWithSongs(albumId: Long): Flow<AlbumWithSongs?>

    /**
     * The album's tracks the device kept on their own — played, liked, queued, found in a search —
     * whether or not the album itself was ever looked up.
     *
     * @return the saved tracks whose album is [albumId], in no particular order.
     */
    @Query("SELECT * FROM songs WHERE albumId = :albumId")
    fun observeSavedSongs(albumId: Long): Flow<List<SongEntity>>

    @Query("SELECT cachedAt FROM albums WHERE id = :albumId")
    suspend fun findCachedAt(albumId: Long): Long?

    @Query("SELECT * FROM album_track_order WHERE albumId = :albumId")
    fun observeTrackOrder(albumId: Long): Flow<List<AlbumTrackOrderEntity>>

    @Query("DELETE FROM album_track_order WHERE albumId = :albumId")
    suspend fun deleteTrackOrder(albumId: Long)

    @Insert
    suspend fun insertTrackOrder(entries: List<AlbumTrackOrderEntity>)

    /** The whole order is written at once, so a track missing from [songIds] loses its place. */
    @Transaction
    suspend fun replaceTrackOrder(
        albumId: Long,
        songIds: List<Long>,
    ) {
        deleteTrackOrder(albumId)
        insertTrackOrder(
            songIds.mapIndexed { position, songId ->
                AlbumTrackOrderEntity(albumId = albumId, songId = songId, position = position)
            },
        )
    }
}
