package com.quare.tunescout.feature.album.data.repository

import com.google.common.truth.Truth.assertThat
import com.quare.tunescout.core.database.AlbumLocalDataSource
import com.quare.tunescout.core.model.Album
import com.quare.tunescout.core.network.ITunesRemoteDataSource
import com.quare.tunescout.core.network.RemoteException
import com.quare.tunescout.core.testing.fixture.album
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AlbumRepositoryImplTest {
    private lateinit var repository: AlbumRepositoryImpl
    private lateinit var remoteDataSource: ITunesRemoteDataSource
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

    private fun prepareScenario(
        cached: Album? = null,
        remote: Album? = null,
        failure: RemoteException? = null,
    ) {
        remoteDataSource = mockk {
            coEvery { fetchAlbum(any()) } answers { failure?.let { throw it } ?: remote }
        }
        albumLocalDataSource = mockk(relaxUnitFun = true) {
            every { observe(any()) } returns flowOf(cached)
        }
        repository = AlbumRepositoryImpl(
            remoteDataSource = remoteDataSource,
            albumLocalDataSource = albumLocalDataSource,
        )
    }
}
