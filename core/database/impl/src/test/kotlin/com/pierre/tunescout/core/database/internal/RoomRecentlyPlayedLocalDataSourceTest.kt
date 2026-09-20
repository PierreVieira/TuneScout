package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.RecentlyPlayedDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.RecentlyPlayedEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoomRecentlyPlayedLocalDataSourceTest {
    private var now = 0L
    private lateinit var localDataSource: RoomRecentlyPlayedLocalDataSource

    @Test
    fun `GIVEN songs played one after the other WHEN observing THEN the latest comes first`() = runTest {
        // Given
        prepareScenario()
        localDataSource.record(song(id = 1))
        localDataSource.record(song(id = 2))

        // When
        val observed = localDataSource.observe(limit = 10)

        // Then
        observed.test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(2L, 1L).inOrder()
        }
    }

    @Test
    fun `GIVEN a song already in the list WHEN it plays again THEN it moves to the top once`() = runTest {
        // Given
        prepareScenario()
        localDataSource.record(song(id = 1))
        localDataSource.record(song(id = 2))

        // When
        localDataSource.record(song(id = 1))

        // Then
        localDataSource.observe(limit = 10).test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(1L, 2L).inOrder()
        }
    }

    @Test
    fun `GIVEN more plays than the list keeps WHEN recording THEN the oldest entries are trimmed`() = runTest {
        // Given
        prepareScenario(maxEntries = 2)

        // When
        localDataSource.record(song(id = 1))
        localDataSource.record(song(id = 2))
        localDataSource.record(song(id = 3))

        // Then
        localDataSource.observe(limit = 10).test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(3L, 2L).inOrder()
        }
    }

    @Test
    fun `GIVEN a limit smaller than the list WHEN observing THEN only that many songs come back`() = runTest {
        // Given
        prepareScenario()
        localDataSource.record(song(id = 1))
        localDataSource.record(song(id = 2))

        // When
        val observed = localDataSource.observe(limit = 1)

        // Then
        observed.test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(2L)
        }
    }

    @Test
    fun `GIVEN a recorded song WHEN removing it THEN it is no longer recently played`() = runTest {
        // Given
        prepareScenario()
        localDataSource.record(song(id = 1))

        // When
        localDataSource.remove(songId = 1)

        // Then
        localDataSource.observeIsRecentlyPlayed(songId = 1).test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `GIVEN a recorded song WHEN asking whether it was played THEN it is`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.record(song(id = 1))

        // Then
        localDataSource.observeIsRecentlyPlayed(songId = 1).test {
            assertThat(awaitItem()).isTrue()
        }
    }

    private fun prepareScenario(maxEntries: Int = 20) {
        val songDao = FakeSongDao()
        localDataSource = RoomRecentlyPlayedLocalDataSource(
            recentlyPlayedDao = FakeRecentlyPlayedDao(songs = songDao.songs),
            songDao = songDao,
            timestampProvider = { ++now },
            maxEntries = maxEntries,
        )
    }
}

private class FakeSongDao : SongDao {
    val songs = mutableMapOf<Long, SongEntity>()

    override suspend fun upsertAll(songs: List<SongEntity>) {
        songs.forEach { song -> this.songs[song.id] = song }
    }

    override fun observeById(songId: Long): Flow<SongEntity?> = error("unused")

    override suspend fun getById(songId: Long): SongEntity? = error("unused")
}

private class FakeRecentlyPlayedDao(
    private val songs: Map<Long, SongEntity>,
) : RecentlyPlayedDao {
    private val entries = MutableStateFlow(emptyList<RecentlyPlayedEntity>())

    override fun observeMostRecent(limit: Int): Flow<List<SongEntity>> = entries.map { current ->
        current.sortedByDescending { entry -> entry.playedAt }.take(limit).mapNotNull { entry -> songs[entry.songId] }
    }

    override fun observeContains(songId: Long): Flow<Boolean> = entries.map { current ->
        current.any { entry -> entry.songId == songId }
    }

    override suspend fun upsert(entry: RecentlyPlayedEntity) {
        entries.value = entries.value.filterNot { current -> current.songId == entry.songId } + entry
    }

    override suspend fun trimTo(keep: Int) {
        entries.value = entries.value.sortedByDescending { entry -> entry.playedAt }.take(keep)
    }

    override suspend fun deleteBySongId(songId: Long) {
        entries.value = entries.value.filterNot { entry -> entry.songId == songId }
    }
}
