package com.pierre.tunescout.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.internal.RoomLibrarySearchLocalDataSource
import com.pierre.tunescout.core.model.LibraryItemKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomLibrarySearchLocalDataSourceTest {
    private lateinit var database: TuneScoutDatabase
    private lateinit var dataSource: LibrarySearchLocalDataSource
    private var now: Long = 0

    @BeforeEach
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room
            .inMemoryDatabaseBuilder<TuneScoutDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        dataSource = RoomLibrarySearchLocalDataSource(
            librarySearchDao = database.librarySearchDao(),
            timestampProvider = { ++now },
            maxEntries = MAX_ENTRIES,
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun theMostRecentlyOpenedItemComesFirst() = runTest {
        // Given
        dataSource.record(LibraryItemKey.Playlist(playlistId = 1))
        dataSource.record(LibraryItemKey.Favorites)

        // When
        val keys = dataSource.observeRecent().first()

        // Then
        assertThat(keys)
            .containsExactly(LibraryItemKey.Favorites, LibraryItemKey.Playlist(playlistId = 1))
            .inOrder()
    }

    @Test
    fun openingAnItemAgainMovesItToTheTopWithoutDuplicatingIt() = runTest {
        // Given
        dataSource.record(LibraryItemKey.Playlist(playlistId = 1))
        dataSource.record(LibraryItemKey.Favorites)

        // When
        dataSource.record(LibraryItemKey.Playlist(playlistId = 1))

        // Then
        assertThat(dataSource.observeRecent().first())
            .containsExactly(LibraryItemKey.Playlist(playlistId = 1), LibraryItemKey.Favorites)
            .inOrder()
    }

    @Test
    fun removingOneRecentSearchLeavesTheOthers() = runTest {
        // Given
        dataSource.record(LibraryItemKey.Playlist(playlistId = 1))
        dataSource.record(LibraryItemKey.Favorites)

        // When
        dataSource.remove(LibraryItemKey.Favorites)

        // Then
        assertThat(dataSource.observeRecent().first())
            .containsExactly(LibraryItemKey.Playlist(playlistId = 1))
    }

    @Test
    fun theOldestSearchFallsOffOnceTheCapIsReached() = runTest {
        // When
        (1L..(MAX_ENTRIES + 2L)).forEach { id ->
            dataSource.record(LibraryItemKey.Playlist(playlistId = id))
        }

        // Then
        val keys = dataSource.observeRecent().first()
        assertThat(keys).hasSize(MAX_ENTRIES)
        assertThat(keys).doesNotContain(LibraryItemKey.Playlist(playlistId = 1))
    }

    private companion object {
        const val MAX_ENTRIES = 10
    }
}
