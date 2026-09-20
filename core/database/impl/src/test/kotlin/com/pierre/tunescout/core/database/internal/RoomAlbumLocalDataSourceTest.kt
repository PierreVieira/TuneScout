package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class RoomAlbumLocalDataSourceTest {
    private val cacheMaxAge = 1.hours
    private var now = 0L
    private lateinit var localDataSource: RoomAlbumLocalDataSource

    @Test
    fun `GIVEN a saved album WHEN observing it THEN its tracks come back in track order`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(album(id = 10, songs = listOf(song(id = 2, trackNumber = 2), song(id = 1))))

        // When
        val observed = localDataSource.observe(albumId = 10)

        // Then
        observed.test {
            assertThat(awaitItem()?.songs?.map { song -> song.id }).containsExactly(1L, 2L).inOrder()
        }
    }

    @Test
    fun `GIVEN an album saved moments ago WHEN asking whether it is fresh THEN it is`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(album(id = 10))

        // When
        val isFresh = localDataSource.isFresherThan(albumId = 10, maxAge = cacheMaxAge)

        // Then
        assertThat(isFresh).isTrue()
    }

    @Test
    fun `GIVEN an album saved longer ago than the max age WHEN asking whether it is fresh THEN it is not`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(album(id = 10))
        now += 2.hours.inWholeMilliseconds

        // When
        val isFresh = localDataSource.isFresherThan(albumId = 10, maxAge = cacheMaxAge)

        // Then
        assertThat(isFresh).isFalse()
    }

    @Test
    fun `GIVEN an album saved within the max age WHEN asking whether it is fresh THEN it is`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(album(id = 10))
        now += 30.minutes.inWholeMilliseconds

        // When
        val isFresh = localDataSource.isFresherThan(albumId = 10, maxAge = cacheMaxAge)

        // Then
        assertThat(isFresh).isTrue()
    }

    @Test
    fun `GIVEN an album that was never cached WHEN asking whether it is fresh THEN it is not`() = runTest {
        // Given
        prepareScenario()

        // When
        val isFresh = localDataSource.isFresherThan(albumId = 10, maxAge = cacheMaxAge)

        // Then
        assertThat(isFresh).isFalse()
    }

    private fun prepareScenario() {
        val songDao = FakeAlbumSongDao()
        localDataSource = RoomAlbumLocalDataSource(
            albumDao = FakeAlbumDao(songs = songDao.songs),
            songDao = songDao,
            timestampProvider = { now },
        )
    }
}

private class FakeAlbumSongDao : SongDao {
    val songs = mutableMapOf<Long, SongEntity>()

    override suspend fun upsertAll(songs: List<SongEntity>) {
        songs.forEach { song -> this.songs[song.id] = song }
    }

    override fun observeById(songId: Long): Flow<SongEntity?> = error("unused")

    override suspend fun getById(songId: Long): SongEntity? = error("unused")

    override suspend fun findByTerm(
        term: String,
        limit: Int,
    ): List<SongEntity> = error("unused")

    override suspend fun trimCacheTo(keep: Int) {
        error("unused")
    }
}

private class FakeAlbumDao(
    private val songs: Map<Long, SongEntity>,
) : AlbumDao {
    private val albums = MutableStateFlow(emptyMap<Long, AlbumEntity>())

    override suspend fun upsert(album: AlbumEntity) {
        albums.value = albums.value + (album.id to album)
    }

    override fun observeWithSongs(albumId: Long): Flow<AlbumWithSongs?> = albums.map { current ->
        current[albumId]?.let { album ->
            AlbumWithSongs(
                album = album,
                songs = songs.values.filter { song -> song.albumId == albumId },
            )
        }
    }

    override suspend fun findCachedAt(albumId: Long): Long? = albums.value[albumId]?.cachedAt
}
