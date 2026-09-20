package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.LibrarySearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LibrarySearchDao {
    @Query("SELECT * FROM library_recent_searches ORDER BY searchedAt DESC LIMIT :limit")
    fun observeMostRecent(limit: Int): Flow<List<LibrarySearchEntity>>

    @Upsert
    suspend fun upsert(entry: LibrarySearchEntity)

    @Query("DELETE FROM library_recent_searches WHERE itemId = :itemId")
    suspend fun deleteById(itemId: String)

    @Query(
        """
        DELETE FROM library_recent_searches WHERE itemId NOT IN (
            SELECT itemId FROM library_recent_searches ORDER BY searchedAt DESC LIMIT :keep
        )
        """,
    )
    suspend fun trimTo(keep: Int)

    @Transaction
    suspend fun record(
        entry: LibrarySearchEntity,
        keep: Int,
    ) {
        upsert(entry)
        trimTo(keep)
    }
}
