package com.pierre.tunescout.feature.library.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fake.FakeDownloadLocalDataSource
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveCollectionDownloadsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveDownloadedSongsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ToggleCollectionDownloadUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CollectionDownloadUseCasesTest {
    private lateinit var downloads: FakeDownloadLocalDataSource

    @Test
    fun `GIVEN a playlist not downloaded WHEN toggling it THEN it is asked for`() = runTest {
        // Given
        prepareScenario()

        // When
        ToggleCollectionDownloadUseCase(downloads)(key = LibraryItemKey.Playlist(playlistId = 7), isDownloaded = false)

        // Then
        assertThat(downloads.collections.value).containsExactly(LibraryItemKey.Playlist(playlistId = 7))
    }

    @Test
    fun `GIVEN the liked songs downloaded WHEN toggling them THEN the request is taken back`() = runTest {
        // Given
        prepareScenario(collections = setOf(LibraryItemKey.Favorites))

        // When
        ToggleCollectionDownloadUseCase(downloads)(key = LibraryItemKey.Favorites, isDownloaded = true)

        // Then
        assertThat(downloads.collections.value).isEmpty()
    }

    @Test
    fun `GIVEN collections asked for WHEN observing them THEN they come back`() = runTest {
        // Given
        prepareScenario(collections = setOf(LibraryItemKey.Favorites))

        // When / Then
        ObserveCollectionDownloadsUseCase(downloads)().test {
            assertThat(awaitItem()).containsExactly(LibraryItemKey.Favorites)
        }
    }

    @Test
    fun `GIVEN songs downloaded on their own WHEN observing them THEN they come back`() = runTest {
        // Given
        prepareScenario(ownSongs = listOf(song(id = 2), song(id = 1)))

        // When / Then
        ObserveDownloadedSongsUseCase(downloads)().test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(2L, 1L).inOrder()
        }
    }

    private fun prepareScenario(
        collections: Set<LibraryItemKey> = emptySet(),
        ownSongs: List<Song> = emptyList(),
    ) {
        downloads = FakeDownloadLocalDataSource(collections = collections, ownSongs = ownSongs)
    }
}
