package com.quare.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.quare.tunescout.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SongDao {
    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE id = :songId")
    fun observeById(songId: Long): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getById(songId: Long): SongEntity?
}
