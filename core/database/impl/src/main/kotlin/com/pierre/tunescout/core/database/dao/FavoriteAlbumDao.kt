package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.FavoriteAlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoriteAlbumDao {
    @Query(
        """
        SELECT albums.* FROM albums
        INNER JOIN favorite_albums ON favorite_albums.albumId = albums.id
        ORDER BY favorite_albums.favoritedAt DESC
        """,
    )
    fun observeAll(): Flow<List<AlbumEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_albums WHERE albumId = :albumId)")
    fun observeContains(albumId: Long): Flow<Boolean>

    @Upsert
    suspend fun upsert(entry: FavoriteAlbumEntity)

    @Query("DELETE FROM favorite_albums WHERE albumId = :albumId")
    suspend fun deleteByAlbumId(albumId: Long)
}
