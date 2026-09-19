package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.FavoriteSongDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.FavoriteSongEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoomFavoriteSongLocalDataSourceTest {
    private var now = 0L
    private lateinit var localDataSource: RoomFavoriteSongLocalDataSource

    @Test
    fun `GIVEN songs liked one after the other WHEN observing THEN the latest comes first`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.add(song(id = 1))
        localDataSource.add(song(id = 2))

        // Then
        localDataSource.observeAll().test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(2L, 1L).inOrder()
        }
    }

    @Test
    fun `GIVEN a song that was never liked WHEN asking whether it is THEN it is not`() = runTest {
        // Given
        prepareScenario()

        // When
        val isFavorite = localDataSource.observeIsFavorite(songId = 1)

        // Then
        isFavorite.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `GIVEN a liked song WHEN asking whether it is THEN it is`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.add(song(id = 1))

        // Then
        localDataSource.observeIsFavorite(songId = 1).test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `GIVEN a liked song WHEN unliking it THEN it leaves the list`() = runTest {
        // Given
        prepareScenario()
        localDataSource.add(song(id = 1))
        localDataSource.add(song(id = 2))

        // When
        localDataSource.remove(songId = 1)

        // Then
        localDataSource.observeAll().test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(2L)
        }
    }

    @Test
    fun `GIVEN a song liked twice WHEN observing THEN it is listed once`() = runTest {
        // Given
        prepareScenario()
        localDataSource.add(song(id = 1))

        // When
        localDataSource.add(song(id = 1))

        // Then
        localDataSource.observeAll().test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(1L)
        }
    }

    private fun prepareScenario() {
        val songDao = FakeFavoriteSongDao()
        localDataSource = RoomFavoriteSongLocalDataSource(
            favoriteSongDao = FakeFavoriteDao(songs = songDao.songs),
            songDao = songDao,
            timestampProvider = { ++now },
        )
    }
}

private class FakeFavoriteSongDao : SongDao {
    val songs = mutableMapOf<Long, SongEntity>()

    override suspend fun upsertAll(songs: List<SongEntity>) {
        songs.forEach { song -> this.songs[song.id] = song }
    }

    override fun observeById(songId: Long): Flow<SongEntity?> = error("unused")

    override suspend fun getById(songId: Long): SongEntity? = error("unused")
}

private class FakeFavoriteDao(
    private val songs: Map<Long, SongEntity>,
) : FavoriteSongDao {
    private val entries = MutableStateFlow(emptyList<FavoriteSongEntity>())

    override fun observeAll(): Flow<List<SongEntity>> = entries.map { current ->
        current
            .sortedByDescending { entry -> entry.favoritedAt }
            .mapNotNull { entry -> songs[entry.songId] }
    }

    override fun observeContains(songId: Long): Flow<Boolean> = entries.map { current ->
        current.any { entry -> entry.songId == songId }
    }

    override suspend fun upsert(entry: FavoriteSongEntity) {
        entries.value = entries.value.filterNot { current -> current.songId == entry.songId } + entry
    }

    override suspend fun deleteBySongId(songId: Long) {
        entries.value = entries.value.filterNot { entry -> entry.songId == songId }
    }
}
