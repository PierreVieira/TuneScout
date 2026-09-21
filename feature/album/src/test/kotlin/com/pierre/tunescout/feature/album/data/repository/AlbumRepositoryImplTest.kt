package com.pierre.tunescout.feature.album.data.repository

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.network.RemoteException
import com.pierre.tunescout.core.testing.fixture.album
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class AlbumRepositoryImplTest {
    private val cacheMaxAge = 1.hours
    private lateinit var repository: AlbumRepositoryImpl
    private lateinit var remoteDataSource: AlbumRemoteDataSource
    private lateinit var albumLocalDataSource: AlbumLocalDataSource

    @Test
    fun `GIVEN a cached album WHEN observing THEN emits the local album`() = runTest {
        // Given
        prepareScenario(cached = album(id = 10))

        // When
        val album = repository.observeAlbum(albumId = 10).first()

        // Then
        assertThat(album?.id).isEqualTo(10L)
    }

    @Test
    fun `GIVEN the album exists remotely WHEN refreshing THEN saves it locally and succeeds`() = runTest {
        // Given
        prepareScenario(remote = album(id = 10))

        // When
        val result = repository.refreshAlbum(albumId = 10)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { albumLocalDataSource.save(album(id = 10)) }
    }

    @Test
    fun `GIVEN the album is missing remotely WHEN refreshing THEN fails with AlbumNotFoundException`() = runTest {
        // Given
        prepareScenario(remote = null)

        // When
        val result = repository.refreshAlbum(albumId = 10)

        // Then
        assertThat(result.exceptionOrNull()).isInstanceOf(AlbumNotFoundException::class.java)
    }

    @Test
    fun `GIVEN the API is throttling WHEN refreshing THEN fails with the remote exception`() = runTest {
        // Given
        prepareScenario(failure = RemoteException.RateLimited(cause = null))

        // When
        val result = repository.refreshAlbum(albumId = 10)

        // Then
        assertThat(result.exceptionOrNull()).isInstanceOf(RemoteException.RateLimited::class.java)
    }

    @Test
    fun `GIVEN an album cached moments ago WHEN refreshing THEN keeps it without calling the API`() = runTest {
        // Given
        prepareScenario(cached = album(id = 10), isFresh = true, remote = album(id = 10))

        // When
        val result = repository.refreshAlbum(albumId = 10)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 0) { remoteDataSource.fetchAlbum(any()) }
        coVerify(exactly = 0) { albumLocalDataSource.save(any()) }
    }

    @Test
    fun `GIVEN an album cached long ago WHEN refreshing THEN asks the API for it again`() = runTest {
        // Given
        prepareScenario(cached = album(id = 10), isFresh = false, remote = album(id = 10))

        // When
        repository.refreshAlbum(albumId = 10)

        // Then
        coVerify { remoteDataSource.fetchAlbum(10) }
    }

    @Test
    fun `GIVEN a cached album WHEN refreshing THEN measures its age against the configured window`() = runTest {
        // Given
        prepareScenario(cached = album(id = 10), isFresh = true)

        // When
        repository.refreshAlbum(albumId = 10)

        // Then
        coVerify { albumLocalDataSource.isFresherThan(albumId = 10, maxAge = cacheMaxAge) }
    }

    @Test
    fun `GIVEN the network state WHEN observing whether it is online THEN relays the monitor`() = runTest {
        // Given
        prepareScenario(isOnline = false)

        // When
        val isOnline = repository.observeIsOnline().first()

        // Then
        assertThat(isOnline).isFalse()
    }

    @Test
    fun `WHEN saving a track order THEN hands it to the album store`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.saveTrackOrder(albumId = 10, songIds = listOf(2, 1))

        // Then
        coVerify { albumLocalDataSource.saveTrackOrder(albumId = 10, songIds = listOf(2L, 1L)) }
    }

    private fun prepareScenario(
        cached: Album? = null,
        remote: Album? = null,
        failure: RemoteException? = null,
        isFresh: Boolean = false,
        isOnline: Boolean = true,
    ) {
        remoteDataSource = mockk {
            coEvery { fetchAlbum(any()) } answers { failure?.let { throw it } ?: remote }
        }
        albumLocalDataSource = mockk(relaxUnitFun = true) {
            every { observe(any()) } returns flowOf(cached)
            coEvery { isFresherThan(any(), any<Duration>()) } returns isFresh
        }
        repository = AlbumRepositoryImpl(
            remoteDataSource = remoteDataSource,
            albumLocalDataSource = albumLocalDataSource,
            networkMonitor = NetworkMonitor { flowOf(isOnline) },
            cacheMaxAge = cacheMaxAge,
        )
    }
}
