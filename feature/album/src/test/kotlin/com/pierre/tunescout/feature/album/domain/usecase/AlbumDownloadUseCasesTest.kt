package com.pierre.tunescout.feature.album.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.testing.fake.FakeDownloadLocalDataSource
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.feature.album.domain.usecase.impl.ObserveCollectionDownloadsUseCase
import com.pierre.tunescout.feature.album.domain.usecase.impl.ToggleAlbumDownloadUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AlbumDownloadUseCasesTest {
    private val albumKey = LibraryItemKey.Album(albumId = 10)
    private lateinit var downloads: FakeDownloadLocalDataSource
    private lateinit var favorites: FavoriteAlbumLocalDataSource
    private lateinit var toggleAlbumDownload: ToggleAlbumDownloadUseCase

    @Test
    fun `GIVEN an album not in the library WHEN downloading it THEN it is asked for and joins the library`() = runTest {
        // Given
        prepareScenario()
        val album = album(id = 10)

        // When
        toggleAlbumDownload(album = album, isDownloaded = false, isFavorite = false)

        // Then
        assertThat(downloads.collections.value).containsExactly(albumKey)
        coVerify { favorites.add(album) }
    }

    @Test
    fun `GIVEN an album already in the library WHEN downloading it THEN it is not added again`() = runTest {
        // Given
        prepareScenario()

        // When
        toggleAlbumDownload(album = album(id = 10), isDownloaded = false, isFavorite = true)

        // Then
        assertThat(downloads.collections.value).containsExactly(albumKey)
        coVerify(exactly = 0) { favorites.add(any()) }
    }

    @Test
    fun `GIVEN an album put together from the saved tracks WHEN downloading it THEN it is asked for but not liked`() =
        runTest {
            // Given
            prepareScenario()

            // When
            toggleAlbumDownload(album = album(id = 10, isComplete = false), isDownloaded = false, isFavorite = false)

            // Then
            assertThat(downloads.collections.value).containsExactly(albumKey)
            coVerify(exactly = 0) { favorites.add(any()) }
        }

    @Test
    fun `GIVEN a downloaded album WHEN toggling it THEN the request is taken back and the album stays liked`() =
        runTest {
            // Given
            prepareScenario(collections = setOf(albumKey))

            // When
            toggleAlbumDownload(album = album(id = 10), isDownloaded = true, isFavorite = true)

            // Then
            assertThat(downloads.collections.value).isEmpty()
            coVerify(exactly = 0) { favorites.remove(any()) }
        }

    @Test
    fun `GIVEN collections asked for WHEN observing them THEN they are the ones the device keeps`() = runTest {
        // Given
        prepareScenario(collections = setOf(albumKey, LibraryItemKey.Favorites))

        // When / Then
        ObserveCollectionDownloadsUseCase(downloads)().test {
            assertThat(awaitItem()).containsExactly(albumKey, LibraryItemKey.Favorites)
        }
    }

    private fun prepareScenario(collections: Set<LibraryItemKey> = emptySet()) {
        downloads = FakeDownloadLocalDataSource(collections = collections)
        favorites = mockk(relaxUnitFun = true)
        toggleAlbumDownload = ToggleAlbumDownloadUseCase(
            downloadLocalDataSource = downloads,
            favoriteAlbumLocalDataSource = favorites,
        )
    }
}
