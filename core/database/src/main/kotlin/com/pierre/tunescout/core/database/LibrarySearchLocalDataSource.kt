package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.LibraryItemKey
import kotlinx.coroutines.flow.Flow

interface LibrarySearchLocalDataSource {
    fun observeRecent(): Flow<List<LibraryItemKey>>

    suspend fun record(key: LibraryItemKey)

    suspend fun remove(key: LibraryItemKey)
}
