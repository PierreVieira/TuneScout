package com.quare.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.quare.tunescout.core.database.entity.AlbumEntity
import com.quare.tunescout.core.database.relation.AlbumWithSongs
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AlbumDao {
    @Upsert
    suspend fun upsert(album: AlbumEntity)

    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    fun observeWithSongs(albumId: Long): Flow<AlbumWithSongs?>
}
