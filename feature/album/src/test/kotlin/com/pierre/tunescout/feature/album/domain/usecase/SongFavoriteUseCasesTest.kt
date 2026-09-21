package com.pierre.tunescout.feature.album.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.testing.fake.FakeFavoriteSongLocalDataSource
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.album.domain.usecase.impl.ObserveFavoriteSongIdsUseCase
import com.pierre.tunescout.feature.album.domain.usecase.impl.ToggleSongFavoriteUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SongFavoriteUseCasesTest {
    private lateinit var favoriteSongLocalDataSource: FakeFavoriteSongLocalDataSource

    @Test
    fun `GIVEN liked songs WHEN observing their ids THEN emits the ids alone`() = runTest {
        // Given
        prepareScenario(favoriteIds = listOf(1, 2))

        // When
        val observed = ObserveFavoriteSongIdsUseCase(favoriteSongLocalDataSource)()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactly(1L, 2L)
        }
    }

    @Test
    fun `GIVEN a song that is not liked WHEN toggling it THEN it is saved`() = runTest {
        // Given
        prepareScenario()
        val song = song(id = 1)

        // When
        ToggleSongFavoriteUseCase(favoriteSongLocalDataSource)(song = song, isFavorite = false)

        // Then
        assertThat(favoriteSongLocalDataSource.added).containsExactly(song)
        assertThat(favoriteSongLocalDataSource.removedSongIds).isEmpty()
    }

    @Test
    fun `GIVEN a liked song WHEN toggling it THEN it is dropped`() = runTest {
        // Given
        prepareScenario(favoriteIds = listOf(1))

        // When
        ToggleSongFavoriteUseCase(favoriteSongLocalDataSource)(song = song(id = 1), isFavorite = true)

        // Then
        assertThat(favoriteSongLocalDataSource.removedSongIds).containsExactly(1L)
        assertThat(favoriteSongLocalDataSource.added).isEmpty()
    }

    private fun prepareScenario(favoriteIds: List<Long> = emptyList()) {
        favoriteSongLocalDataSource = FakeFavoriteSongLocalDataSource(
            favorites = favoriteIds.map { id -> song(id = id) },
        )
    }
}
