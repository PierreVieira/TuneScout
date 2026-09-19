package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.PlaybackQueueEntity
import com.pierre.tunescout.core.database.entity.PlaybackSessionEntity
import com.pierre.tunescout.core.database.entity.SongEntity

@Dao
internal interface PlaybackSessionDao {
    @Upsert
    suspend fun upsertSession(session: PlaybackSessionEntity)

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM playback_queue")
    suspend fun clearQueue()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(entries: List<PlaybackQueueEntity>)

    @Query("SELECT * FROM playback_session LIMIT 1")
    suspend fun findSession(): PlaybackSessionEntity?

    @Query("SELECT * FROM playback_queue ORDER BY position ASC")
    suspend fun findQueue(): List<PlaybackQueueEntity>

    @Query("SELECT * FROM songs WHERE id IN (:songIds)")
    suspend fun findSongs(songIds: List<Long>): List<SongEntity>

    @Transaction
    suspend fun save(
        session: PlaybackSessionEntity,
        entries: List<PlaybackQueueEntity>,
        songs: List<SongEntity>,
    ) {
        upsertSongs(songs)
        clearQueue()
        insertQueue(entries)
        upsertSession(session)
    }
}
