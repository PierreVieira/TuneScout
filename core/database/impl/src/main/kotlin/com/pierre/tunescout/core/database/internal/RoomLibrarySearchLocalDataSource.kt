package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.LibrarySearchLocalDataSource
import com.pierre.tunescout.core.database.dao.LibrarySearchDao
import com.pierre.tunescout.core.database.entity.LibrarySearchEntity
import com.pierre.tunescout.core.database.mapper.toItemId
import com.pierre.tunescout.core.database.mapper.toLibraryItemKeyOrNull
import com.pierre.tunescout.core.model.LibraryItemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomLibrarySearchLocalDataSource(
    private val librarySearchDao: LibrarySearchDao,
    private val timestampProvider: TimestampProvider,
    private val maxEntries: Int,
) : LibrarySearchLocalDataSource {
    override fun observeRecent(): Flow<List<LibraryItemKey>> = librarySearchDao
        .observeMostRecent(maxEntries)
        .map { entries -> entries.mapNotNull { entry -> entry.itemId.toLibraryItemKeyOrNull() } }

    override suspend fun record(key: LibraryItemKey) {
        librarySearchDao.record(
            entry = LibrarySearchEntity(itemId = key.toItemId(), searchedAt = timestampProvider.provide()),
            keep = maxEntries,
        )
    }

    override suspend fun remove(key: LibraryItemKey) {
        librarySearchDao.deleteById(key.toItemId())
    }
}
