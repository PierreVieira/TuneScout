package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.FavoriteSongEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoriteSongDao {
    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN favorite_songs ON favorite_songs.songId = songs.id
        ORDER BY favorite_songs.favoritedAt DESC
        """,
    )
    fun observeAll(): Flow<List<SongEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    fun observeContains(songId: Long): Flow<Boolean>

    @Upsert
    suspend fun upsert(entry: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteBySongId(songId: Long)
}
