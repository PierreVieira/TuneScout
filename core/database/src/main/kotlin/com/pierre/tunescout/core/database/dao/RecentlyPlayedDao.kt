package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.RecentlyPlayedEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RecentlyPlayedDao {
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN recently_played ON recently_played.songId = songs.id
        ORDER BY recently_played.playedAt DESC
        LIMIT :limit
        """,
    )
    fun observeMostRecent(limit: Int): Flow<List<SongEntity>>

    @Upsert
    suspend fun upsert(entry: RecentlyPlayedEntity)

    @Query(
        """
        DELETE FROM recently_played WHERE songId NOT IN (
            SELECT songId FROM recently_played ORDER BY playedAt DESC LIMIT :keep
        )
        """,
    )
    suspend fun trimTo(keep: Int)

    @Query("DELETE FROM recently_played WHERE songId = :songId")
    suspend fun deleteBySongId(songId: Long)

    @Transaction
    suspend fun record(
        entry: RecentlyPlayedEntity,
        keep: Int,
    ) {
        upsert(entry)
        trimTo(keep)
    }
}
