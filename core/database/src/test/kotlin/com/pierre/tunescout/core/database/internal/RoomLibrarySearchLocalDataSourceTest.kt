package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.LibrarySearchDao
import com.pierre.tunescout.core.database.entity.LibrarySearchEntity
import com.pierre.tunescout.core.model.LibraryItemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoomLibrarySearchLocalDataSourceTest {
    private var now = 0L
    private lateinit var localDataSource: RoomLibrarySearchLocalDataSource

    @Test
    fun `GIVEN items opened one after the other WHEN observing THEN the latest comes first`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.record(LibraryItemKey.Playlist(playlistId = 7))
        localDataSource.record(LibraryItemKey.Favorites)
        localDataSource.record(LibraryItemKey.Album(albumId = 3))

        // Then
        localDataSource.observeRecent().test {
            assertThat(awaitItem())
                .containsExactly(
                    LibraryItemKey.Album(albumId = 3),
                    LibraryItemKey.Favorites,
                    LibraryItemKey.Playlist(playlistId = 7),
                ).inOrder()
        }
    }

    @Test
    fun `GIVEN an item already searched WHEN opening it again THEN it moves to the top once`() = runTest {
        // Given
        prepareScenario()
        localDataSource.record(LibraryItemKey.Playlist(playlistId = 7))
        localDataSource.record(LibraryItemKey.Favorites)

        // When
        localDataSource.record(LibraryItemKey.Playlist(playlistId = 7))

        // Then
        localDataSource.observeRecent().test {
            assertThat(awaitItem())
                .containsExactly(LibraryItemKey.Playlist(playlistId = 7), LibraryItemKey.Favorites)
                .inOrder()
        }
    }

    @Test
    fun `GIVEN more searches than the list keeps WHEN recording THEN the oldest are trimmed`() = runTest {
        // Given
        prepareScenario(maxEntries = 2)

        // When
        localDataSource.record(LibraryItemKey.Playlist(playlistId = 1))
        localDataSource.record(LibraryItemKey.Playlist(playlistId = 2))
        localDataSource.record(LibraryItemKey.Playlist(playlistId = 3))

        // Then
        localDataSource.observeRecent().test {
            assertThat(awaitItem())
                .containsExactly(LibraryItemKey.Playlist(playlistId = 3), LibraryItemKey.Playlist(playlistId = 2))
                .inOrder()
        }
    }

    @Test
    fun `GIVEN a recent search WHEN removing it THEN the others stay`() = runTest {
        // Given
        prepareScenario()
        localDataSource.record(LibraryItemKey.Favorites)
        localDataSource.record(LibraryItemKey.Album(albumId = 3))

        // When
        localDataSource.remove(LibraryItemKey.Favorites)

        // Then
        localDataSource.observeRecent().test {
            assertThat(awaitItem()).containsExactly(LibraryItemKey.Album(albumId = 3))
        }
    }

    @Test
    fun `GIVEN a stored id that no longer names anything WHEN observing THEN it is left out`() = runTest {
        // Given
        val dao = FakeLibrarySearchDao()
        prepareScenario(dao = dao)
        localDataSource.record(LibraryItemKey.Favorites)

        // When
        dao.upsert(LibrarySearchEntity(itemId = "artist:9", searchedAt = 99))

        // Then
        localDataSource.observeRecent().test {
            assertThat(awaitItem()).containsExactly(LibraryItemKey.Favorites)
        }
    }

    private fun prepareScenario(
        maxEntries: Int = 20,
        dao: FakeLibrarySearchDao = FakeLibrarySearchDao(),
    ) {
        localDataSource = RoomLibrarySearchLocalDataSource(
            librarySearchDao = dao,
            timestampProvider = { ++now },
            maxEntries = maxEntries,
        )
    }
}

/**
 * Implements only the members the DAO declares, so `record` — the one with a body of its own, which
 * writes and then trims — runs for real.
 */
private class FakeLibrarySearchDao : LibrarySearchDao {
    private val entries = MutableStateFlow(emptyList<LibrarySearchEntity>())

    override fun observeMostRecent(limit: Int): Flow<List<LibrarySearchEntity>> = entries.map { current ->
        current.mostRecent(limit)
    }

    override suspend fun upsert(entry: LibrarySearchEntity) {
        entries.value = entries.value.filterNot { current -> current.itemId == entry.itemId } + entry
    }

    override suspend fun deleteById(itemId: String) {
        entries.value = entries.value.filterNot { entry -> entry.itemId == itemId }
    }

    override suspend fun trimTo(keep: Int) {
        entries.value = entries.value.mostRecent(keep)
    }

    private fun List<LibrarySearchEntity>.mostRecent(limit: Int): List<LibrarySearchEntity> =
        sortedByDescending { entry -> entry.searchedAt }.take(limit)
}
