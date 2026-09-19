package com.pierre.tunescout.feature.songoptions.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.IsFavoriteUseCase
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SongFavoriteUseCasesTest {
    private lateinit var favoriteSongLocalDataSource: FakeFavoriteSongLocalDataSource

    @Test
    fun `GIVEN a liked song WHEN asking whether it is THEN it is`() = runTest {
        // Given
        prepareScenario(isFavorite = true)

        // When
        val observed = IsFavoriteUseCase(favoriteSongLocalDataSource)(songId = 1)

        // Then
        observed.test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
        assertThat(favoriteSongLocalDataSource.observedSongIds).containsExactly(1L)
    }

    @Test
    fun `GIVEN a song that is not liked WHEN toggling it THEN it is saved`() = runTest {
        // Given
        prepareScenario()
        val song = song(id = 1)

        // When
        ToggleFavoriteUseCase(favoriteSongLocalDataSource)(song = song, isFavorite = false)

        // Then
        assertThat(favoriteSongLocalDataSource.added).containsExactly(song)
        assertThat(favoriteSongLocalDataSource.removedSongIds).isEmpty()
    }

    @Test
    fun `GIVEN a liked song WHEN toggling it THEN it is dropped`() = runTest {
        // Given
        prepareScenario()

        // When
        ToggleFavoriteUseCase(favoriteSongLocalDataSource)(song = song(id = 1), isFavorite = true)

        // Then
        assertThat(favoriteSongLocalDataSource.removedSongIds).containsExactly(1L)
        assertThat(favoriteSongLocalDataSource.added).isEmpty()
    }

    private fun prepareScenario(isFavorite: Boolean = false) {
        favoriteSongLocalDataSource = FakeFavoriteSongLocalDataSource(isFavorite = isFavorite)
    }
}

private class FakeFavoriteSongLocalDataSource(
    private val isFavorite: Boolean,
) : FavoriteSongLocalDataSource {
    val observedSongIds = mutableListOf<Long>()
    val added = mutableListOf<Song>()
    val removedSongIds = mutableListOf<Long>()

    override fun observeAll(): Flow<List<Song>> = error("unused")

    override fun observeIsFavorite(songId: Long): Flow<Boolean> {
        observedSongIds += songId
        return flowOf(isFavorite)
    }

    override suspend fun add(song: Song) {
        added += song
    }

    override suspend fun remove(songId: Long) {
        removedSongIds += songId
    }
}
