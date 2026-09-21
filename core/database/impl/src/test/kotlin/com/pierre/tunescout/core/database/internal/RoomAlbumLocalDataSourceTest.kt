package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.AlbumTrackOrderEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class RoomAlbumLocalDataSourceTest {
    private val cacheMaxAge = 1.hours
    private var now = 0L
    private lateinit var localDataSource: RoomAlbumLocalDataSource
    private lateinit var songDao: FakeAlbumSongDao

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
    fun `GIVEN only some tracks of an album never looked up WHEN observing it THEN builds a partial album from them`() =
        runTest {
            // Given
            prepareScenario()
            songDao.upsertAll(
                listOf(
                    song(id = 3, albumId = 10, albumTitle = "Clarity", trackNumber = 3).toEntity(cachedAt = 0),
                    song(id = 1, albumId = 10, albumTitle = "Clarity", trackNumber = 1).toEntity(cachedAt = 0),
                    song(id = 7, albumId = 99, trackNumber = 1).toEntity(cachedAt = 0),
                ),
            )

            // When
            val observed = localDataSource.observe(albumId = 10)

            // Then
            observed.test {
                val album = awaitItem()
                assertThat(album?.title).isEqualTo("Clarity")
                assertThat(album?.isComplete).isFalse()
                assertThat(album?.songs?.map { song -> song.id }).containsExactly(1L, 3L).inOrder()
            }
        }

    @Test
    fun `GIVEN a partial album on screen WHEN the whole album is saved THEN emits the complete one`() = runTest {
        // Given
        prepareScenario()
        songDao.upsertAll(listOf(song(id = 1, albumId = 10).toEntity(cachedAt = 0)))

        // When
        localDataSource.observe(albumId = 10).test {
            assertThat(awaitItem()?.isComplete).isFalse()
            localDataSource.save(album(id = 10))

            // Then
            val album = expectMostRecentItem()
            assertThat(album?.isComplete).isTrue()
            assertThat(album?.songs?.map { song -> song.id }).containsExactly(1L, 2L).inOrder()
        }
    }

    @Test
    fun `GIVEN a saved album WHEN saving a track order THEN its tracks come back in that order`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(album(id = 10, songs = listOf(song(id = 1), song(id = 2, trackNumber = 2))))

        // When
        localDataSource.saveTrackOrder(albumId = 10, songIds = listOf(2, 1))

        // Then
        localDataSource.observe(albumId = 10).test {
            assertThat(awaitItem()?.songs?.map { song -> song.id }).containsExactly(2L, 1L).inOrder()
        }
    }

    @Test
    fun `GIVEN a reordered album WHEN it is saved again THEN keeps the order the user chose`() = runTest {
        // Given
        prepareScenario()
        localDataSource.save(album(id = 10, songs = listOf(song(id = 1), song(id = 2, trackNumber = 2))))
        localDataSource.saveTrackOrder(albumId = 10, songIds = listOf(2, 1))

        // When
        localDataSource.save(
            album(id = 10, songs = listOf(song(id = 1), song(id = 2, trackNumber = 2), song(id = 3, trackNumber = 3))),
        )

        // Then
        localDataSource.observe(albumId = 10).test {
            assertThat(awaitItem()?.songs?.map { song -> song.id }).containsExactly(2L, 1L, 3L).inOrder()
        }
    }

    @Test
    fun `GIVEN only some tracks of an album WHEN saving a track order THEN the partial album follows it`() = runTest {
        // Given
        prepareScenario()
        songDao.upsertAll(
            listOf(
                song(id = 1, albumId = 10, trackNumber = 1).toEntity(cachedAt = 0),
                song(id = 3, albumId = 10, trackNumber = 3).toEntity(cachedAt = 0),
            ),
        )

        // When
        localDataSource.saveTrackOrder(albumId = 10, songIds = listOf(3, 1))

        // Then
        localDataSource.observe(albumId = 10).test {
            assertThat(awaitItem()?.songs?.map { song -> song.id }).containsExactly(3L, 1L).inOrder()
        }
    }

    @Test
    fun `GIVEN no album and none of its tracks WHEN observing it THEN emits null`() = runTest {
        // Given
        prepareScenario()

        // When
        val observed = localDataSource.observe(albumId = 10)

        // Then
        observed.test {
            assertThat(awaitItem()).isNull()
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
        songDao = FakeAlbumSongDao()
        localDataSource = RoomAlbumLocalDataSource(
            albumDao = FakeAlbumDao(songs = songDao.songs),
            songDao = songDao,
            timestampProvider = { now },
        )
    }
}

private class FakeAlbumSongDao : SongDao {
    val songs = MutableStateFlow(emptyMap<Long, SongEntity>())

    override suspend fun upsertAll(songs: List<SongEntity>) {
        this.songs.value = this.songs.value + songs.associateBy { song -> song.id }
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
    private val songs: StateFlow<Map<Long, SongEntity>>,
) : AlbumDao {
    private val albums = MutableStateFlow(emptyMap<Long, AlbumEntity>())

    override suspend fun upsert(album: AlbumEntity) {
        albums.value = albums.value + (album.id to album)
    }

    override fun observeWithSongs(albumId: Long): Flow<AlbumWithSongs?> =
        combine(albums, observeSavedSongs(albumId)) { current, albumSongs ->
            current[albumId]?.let { album -> AlbumWithSongs(album = album, songs = albumSongs) }
        }

    override fun observeSavedSongs(albumId: Long): Flow<List<SongEntity>> =
        songs.map { current -> current.values.filter { song -> song.albumId == albumId } }

    override suspend fun findCachedAt(albumId: Long): Long? = albums.value[albumId]?.cachedAt

    private val trackOrder = MutableStateFlow(emptyList<AlbumTrackOrderEntity>())

    override fun observeTrackOrder(albumId: Long): Flow<List<AlbumTrackOrderEntity>> =
        trackOrder.map { entries -> entries.filter { entry -> entry.albumId == albumId } }

    override suspend fun deleteTrackOrder(albumId: Long) {
        trackOrder.value = trackOrder.value.filterNot { entry -> entry.albumId == albumId }
    }

    override suspend fun insertTrackOrder(entries: List<AlbumTrackOrderEntity>) {
        trackOrder.value += entries
    }
}
