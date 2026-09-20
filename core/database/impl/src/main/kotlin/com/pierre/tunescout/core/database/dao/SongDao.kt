package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SongDao {
    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE id = :songId")
    fun observeById(songId: Long): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getById(songId: Long): SongEntity?

    @Query(
        """
        SELECT * FROM songs
        WHERE title LIKE '%' || :term || '%'
            OR artistName LIKE '%' || :term || '%'
            OR albumTitle LIKE '%' || :term || '%'
        ORDER BY cachedAt DESC, title ASC
        LIMIT :limit
        """,
    )
    suspend fun findByTerm(
        term: String,
        limit: Int,
    ): List<SongEntity>

    /**
     * Drops the songs no other table points at, oldest cached first, once there are more than
     * [keep] of them. A song that is in the history, in a playlist, liked, queued or part of a
     * cached album is what the user can still reach offline, so it is never a candidate.
     */
    @Query(
        """
        DELETE FROM songs WHERE id IN (
            SELECT id FROM songs
            WHERE id NOT IN (SELECT songId FROM recently_played)
                AND id NOT IN (SELECT songId FROM favorite_songs)
                AND id NOT IN (SELECT songId FROM playlist_songs)
                AND id NOT IN (SELECT songId FROM playback_queue)
                AND albumId NOT IN (SELECT id FROM albums)
            ORDER BY cachedAt DESC
            LIMIT -1 OFFSET :keep
        )
        """,
    )
    suspend fun trimCacheTo(keep: Int)

    @Transaction
    suspend fun upsertAllAndTrim(
        songs: List<SongEntity>,
        keep: Int,
    ) {
        upsertAll(songs)
        trimCacheTo(keep)
    }
}
