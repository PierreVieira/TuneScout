package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.DownloadDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.DownloadedCollectionEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongExclusionEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoomDownloadLocalDataSourceTest {
    private var now = 0L
    private val songDao = mockk<SongDao> { coEvery { upsertAll(any()) } just runs }
    private lateinit var downloadDao: FakeDownloadDao
    private lateinit var localDataSource: RoomDownloadLocalDataSource

    @Test
    fun `GIVEN a song only seen in a search WHEN asking to keep it THEN it is saved and requested`() = runTest {
        // Given
        prepareScenario()
        val searched = song(id = 1)

        // When
        localDataSource.addSong(searched)

        // Then
        coVerify { songDao.upsertAll(listOf(searched.toEntity(cachedAt = 1))) }
        assertThat(downloadDao.songs.value).containsExactly(DownloadedSongEntity(songId = 1, requestedAt = 2))
    }

    @Test
    fun `GIVEN a requested song WHEN taking the request back THEN it is no longer asked for`() = runTest {
        // Given
        prepareScenario()
        localDataSource.addSong(song(id = 1))

        // When
        localDataSource.removeSong(songId = 1)

        // Then
        assertThat(downloadDao.songs.value).isEmpty()
    }

    @Test
    fun `GIVEN a requested song WHEN taking the request back THEN it is excluded so a collection cannot keep it`() =
        runTest {
            // Given
            prepareScenario()
            localDataSource.addSong(song(id = 1))

            // When
            localDataSource.removeSong(songId = 1)

            // Then
            assertThat(downloadDao.exclusions.value).containsExactly(1L)
        }

    @Test
    fun `GIVEN an excluded song WHEN asking for it again THEN it is no longer excluded`() = runTest {
        // Given
        prepareScenario()
        localDataSource.addSong(song(id = 1))
        localDataSource.removeSong(songId = 1)

        // When
        localDataSource.addSong(song(id = 1))

        // Then
        assertThat(downloadDao.exclusions.value).isEmpty()
    }

    @Test
    fun `GIVEN collections asked for WHEN observing them THEN each one comes back as the key it was asked with`() =
        runTest {
            // Given
            prepareScenario()

            // When
            localDataSource.addCollection(LibraryItemKey.Album(albumId = 10))
            localDataSource.addCollection(LibraryItemKey.Playlist(playlistId = 7))
            localDataSource.addCollection(LibraryItemKey.Favorites)

            // Then
            localDataSource.observeCollections().test {
                assertThat(awaitItem()).containsExactly(
                    LibraryItemKey.Album(albumId = 10),
                    LibraryItemKey.Playlist(playlistId = 7),
                    LibraryItemKey.Favorites,
                )
            }
        }

    @Test
    fun `GIVEN a collection asked for WHEN taking it back THEN only that one leaves`() = runTest {
        // Given
        prepareScenario()
        localDataSource.addCollection(LibraryItemKey.Album(albumId = 10))
        localDataSource.addCollection(LibraryItemKey.Playlist(playlistId = 7))

        // When
        localDataSource.removeCollection(LibraryItemKey.Album(albumId = 10))

        // Then
        localDataSource.observeCollections().test {
            assertThat(awaitItem()).containsExactly(LibraryItemKey.Playlist(playlistId = 7))
        }
    }

    @Test
    fun `GIVEN the downloaded songs WHEN asking to keep them as a collection THEN nothing is asked for`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.addCollection(LibraryItemKey.DownloadedSongs)
        localDataSource.removeCollection(LibraryItemKey.DownloadedSongs)

        // Then
        localDataSource.observeCollections().test {
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `GIVEN songs downloaded on their own WHEN observing them THEN they come back as songs`() = runTest {
        // Given
        prepareScenario(wanted = listOf(song(id = 2), song(id = 1)))

        // When / Then
        localDataSource.observeOwnSongs().test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(2L, 1L).inOrder()
        }
    }

    @Test
    fun `GIVEN songs the database wants WHEN observing them THEN they come back as songs`() = runTest {
        // Given
        prepareScenario(wanted = listOf(song(id = 1), song(id = 2)))

        // When / Then
        localDataSource.observeWantedSongs().test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(1L, 2L).inOrder()
        }
    }

    @Test
    fun `GIVEN a song the database wants WHEN asking whether it is THEN it is`() = runTest {
        // Given
        prepareScenario(wanted = listOf(song(id = 1)))

        // When / Then
        localDataSource.observeIsWanted(songId = 1).test {
            assertThat(awaitItem()).isTrue()
        }
        localDataSource.observeIsWanted(songId = 2).test {
            assertThat(awaitItem()).isFalse()
        }
    }

    private fun prepareScenario(wanted: List<Song> = emptyList()) {
        downloadDao = FakeDownloadDao(wanted = wanted.map { song -> song.toEntity(cachedAt = 0) })
        localDataSource = RoomDownloadLocalDataSource(
            downloadDao = downloadDao,
            songDao = songDao,
            timestampProvider = { ++now },
        )
    }
}

private class FakeDownloadDao(
    private val wanted: List<SongEntity>,
) : DownloadDao {
    val songs = MutableStateFlow(emptyList<DownloadedSongEntity>())
    val exclusions = MutableStateFlow(emptyList<Long>())
    private val collections = MutableStateFlow(emptyList<DownloadedCollectionEntity>())

    override suspend fun upsertSong(entry: DownloadedSongEntity) {
        songs.value = songs.value.filterNot { current -> current.songId == entry.songId } + entry
    }

    override suspend fun deleteSong(songId: Long) {
        songs.value = songs.value.filterNot { entry -> entry.songId == songId }
    }

    override suspend fun upsertExclusion(entry: DownloadedSongExclusionEntity) {
        exclusions.value = exclusions.value.filterNot { songId -> songId == entry.songId } + entry.songId
    }

    override suspend fun deleteExclusion(songId: Long) {
        exclusions.value = exclusions.value.filterNot { excludedId -> excludedId == songId }
    }

    override suspend fun upsertCollection(entry: DownloadedCollectionEntity) {
        collections.value = collections.value.filterNot { current -> current.isSameAs(entry) } + entry
    }

    override suspend fun deleteCollection(
        kind: String,
        collectionId: Long,
    ) {
        collections.value = collections.value.filterNot { entry ->
            entry.kind == kind && entry.collectionId == collectionId
        }
    }

    override fun observeOwnSongs(): Flow<List<SongEntity>> = MutableStateFlow(wanted)

    override fun observeCollections(): Flow<List<DownloadedCollectionEntity>> = collections

    override fun observeWantedSongs(): Flow<List<SongEntity>> = MutableStateFlow(wanted)

    override fun observeIsWanted(songId: Long): Flow<Boolean> =
        observeWantedSongs().map { songs -> songs.any { song -> song.id == songId } }

    private fun DownloadedCollectionEntity.isSameAs(other: DownloadedCollectionEntity): Boolean =
        kind == other.kind && collectionId == other.collectionId
}
