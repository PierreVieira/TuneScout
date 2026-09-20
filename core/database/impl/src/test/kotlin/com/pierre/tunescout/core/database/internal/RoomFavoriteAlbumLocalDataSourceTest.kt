package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.database.dao.FavoriteAlbumDao
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.FavoriteAlbumEntity
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.testing.fixture.album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration

class RoomFavoriteAlbumLocalDataSourceTest {
    private var now = 0L
    private lateinit var localDataSource: RoomFavoriteAlbumLocalDataSource

    @Test
    fun `GIVEN albums liked one after the other WHEN observing THEN the latest comes first`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.add(album(id = 1))
        localDataSource.add(album(id = 2))

        // Then
        localDataSource.observeAll().test {
            assertThat(awaitItem().map { album -> album.id }).containsExactly(2L, 1L).inOrder()
        }
    }

    @Test
    fun `GIVEN a liked album WHEN observing THEN it carries what a row needs to draw it`() = runTest {
        // Given
        prepareScenario()
        val album = album(id = 1, title = "Discovery", artistName = "Daft Punk")

        // When
        localDataSource.add(album)

        // Then
        localDataSource.observeAll().test {
            val summary = awaitItem().single()
            assertThat(summary.title).isEqualTo("Discovery")
            assertThat(summary.artistName).isEqualTo("Daft Punk")
            assertThat(summary.artwork).isEqualTo(album.artwork)
        }
    }

    @Test
    fun `GIVEN an album that was never liked WHEN asking whether it is THEN it is not`() = runTest {
        // Given
        prepareScenario()

        // When
        val isFavorite = localDataSource.observeIsFavorite(albumId = 1)

        // Then
        isFavorite.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `GIVEN a liked album WHEN asking whether it is THEN it is`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.add(album(id = 1))

        // Then
        localDataSource.observeIsFavorite(albumId = 1).test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `GIVEN a liked album WHEN unliking it THEN it leaves the list`() = runTest {
        // Given
        prepareScenario()
        localDataSource.add(album(id = 1))
        localDataSource.add(album(id = 2))

        // When
        localDataSource.remove(albumId = 1)

        // Then
        localDataSource.observeAll().test {
            assertThat(awaitItem().map { album -> album.id }).containsExactly(2L)
        }
    }

    private fun prepareScenario() {
        val albumLocalDataSource = FakeAlbumLocalDataSource()
        localDataSource = RoomFavoriteAlbumLocalDataSource(
            favoriteAlbumDao = FakeFavoriteAlbumDao(albums = albumLocalDataSource.albums),
            albumLocalDataSource = albumLocalDataSource,
            timestampProvider = { ++now },
        )
    }
}

private class FakeAlbumLocalDataSource : AlbumLocalDataSource {
    val albums = mutableMapOf<Long, AlbumEntity>()

    override suspend fun save(album: Album) {
        albums[album.id] = album.toEntity(cachedAt = 0)
    }

    override fun observe(albumId: Long): Flow<Album?> = error("unused")

    override suspend fun isFresherThan(
        albumId: Long,
        maxAge: Duration,
    ): Boolean = error("unused")
}

private class FakeFavoriteAlbumDao(
    private val albums: Map<Long, AlbumEntity>,
) : FavoriteAlbumDao {
    private val entries = MutableStateFlow(emptyList<FavoriteAlbumEntity>())

    override fun observeAll(): Flow<List<AlbumEntity>> = entries.map { current ->
        current
            .sortedByDescending { entry -> entry.favoritedAt }
            .mapNotNull { entry -> albums[entry.albumId] }
    }

    override fun observeContains(albumId: Long): Flow<Boolean> = entries.map { current ->
        current.any { entry -> entry.albumId == albumId }
    }

    override suspend fun upsert(entry: FavoriteAlbumEntity) {
        entries.value = entries.value.filterNot { current -> current.albumId == entry.albumId } + entry
    }

    override suspend fun deleteByAlbumId(albumId: Long) {
        entries.value = entries.value.filterNot { entry -> entry.albumId == albumId }
    }
}
