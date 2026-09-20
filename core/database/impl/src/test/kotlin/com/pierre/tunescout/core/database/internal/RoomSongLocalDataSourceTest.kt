package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoomSongLocalDataSourceTest {
    private var now = 0L
    private lateinit var localDataSource: RoomSongLocalDataSource
    private lateinit var songDao: FakeSearchableSongDao

    @Test
    fun `GIVEN searched songs WHEN saving them THEN stamps them with the current time`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.save(listOf(song(id = 1), song(id = 2)))

        // Then
        assertThat(songDao.songs.values.map { entity -> entity.cachedAt }).containsExactly(1L, 1L)
    }

    @Test
    fun `GIVEN a cache size WHEN saving songs THEN trims the table down to it`() = runTest {
        // Given
        prepareScenario(maxCachedSongs = 3)

        // When
        localDataSource.save(listOf(song(id = 1)))

        // Then
        assertThat(songDao.trimmedTo).containsExactly(3)
    }

    @Test
    fun `GIVEN cached songs WHEN searching by term THEN asks the database for the matches`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(listOf(song(id = 1, title = "Get Lucky")))

        // When
        val found = localDataSource.findByTerm(term = "lucky", limit = 10)

        // Then
        assertThat(found.map { song -> song.id }).containsExactly(1L)
        assertThat(songDao.requestedLimits).containsExactly(10)
    }

    @Test
    fun `GIVEN a cached song WHEN observing it THEN maps the entity into a song`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(listOf(song(id = 1, title = "Get Lucky")))

        // When
        val observed = localDataSource.observe(songId = 1)

        // Then
        observed.test {
            assertThat(awaitItem()?.title).isEqualTo("Get Lucky")
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN a cached song WHEN finding it by id THEN maps the entity into a song`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(listOf(song(id = 1, title = "Get Lucky")))

        // When
        val found = localDataSource.find(songId = 1)

        // Then
        assertThat(found?.title).isEqualTo("Get Lucky")
    }

    private fun prepareScenario(maxCachedSongs: Int = 500) {
        songDao = FakeSearchableSongDao()
        localDataSource = RoomSongLocalDataSource(
            songDao = songDao,
            timestampProvider = { ++now },
            maxCachedSongs = maxCachedSongs,
        )
    }
}

private class FakeSearchableSongDao : SongDao {
    val songs = mutableMapOf<Long, SongEntity>()
    val trimmedTo = mutableListOf<Int>()
    val requestedLimits = mutableListOf<Int>()

    override suspend fun upsertAll(songs: List<SongEntity>) {
        songs.forEach { song -> this.songs[song.id] = song }
    }

    override fun observeById(songId: Long): Flow<SongEntity?> = flowOf(songs[songId])

    override suspend fun getById(songId: Long): SongEntity? = songs[songId]

    override suspend fun findByTerm(
        term: String,
        limit: Int,
    ): List<SongEntity> {
        requestedLimits += limit
        return songs.values.filter { song -> song.title.contains(term, ignoreCase = true) }.take(limit)
    }

    override suspend fun trimCacheTo(keep: Int) {
        trimmedTo += keep
    }
}
