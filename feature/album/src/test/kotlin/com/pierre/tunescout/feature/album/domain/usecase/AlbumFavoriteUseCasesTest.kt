package com.pierre.tunescout.feature.album.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.feature.album.domain.usecase.impl.IsAlbumFavoriteUseCase
import com.pierre.tunescout.feature.album.domain.usecase.impl.ToggleAlbumFavoriteUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AlbumFavoriteUseCasesTest {
    private lateinit var favoriteAlbumLocalDataSource: FakeFavoriteAlbumLocalDataSource

    @Test
    fun `GIVEN a liked album WHEN asking whether it is THEN it is`() = runTest {
        // Given
        prepareScenario(isFavorite = true)

        // When
        val observed = IsAlbumFavoriteUseCase(favoriteAlbumLocalDataSource)(albumId = 10)

        // Then
        observed.test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
        assertThat(favoriteAlbumLocalDataSource.observedAlbumIds).containsExactly(10L)
    }

    @Test
    fun `GIVEN an album that is not liked WHEN toggling it THEN it is saved`() = runTest {
        // Given
        prepareScenario()
        val album = album(id = 10)

        // When
        ToggleAlbumFavoriteUseCase(favoriteAlbumLocalDataSource)(album = album, isFavorite = false)

        // Then
        assertThat(favoriteAlbumLocalDataSource.added).containsExactly(album)
        assertThat(favoriteAlbumLocalDataSource.removedAlbumIds).isEmpty()
    }

    @Test
    fun `GIVEN a liked album WHEN toggling it THEN it is dropped`() = runTest {
        // Given
        prepareScenario()

        // When
        ToggleAlbumFavoriteUseCase(favoriteAlbumLocalDataSource)(album = album(id = 10), isFavorite = true)

        // Then
        assertThat(favoriteAlbumLocalDataSource.removedAlbumIds).containsExactly(10L)
        assertThat(favoriteAlbumLocalDataSource.added).isEmpty()
    }

    private fun prepareScenario(isFavorite: Boolean = false) {
        favoriteAlbumLocalDataSource = FakeFavoriteAlbumLocalDataSource(isFavorite = isFavorite)
    }
}

private class FakeFavoriteAlbumLocalDataSource(
    private val isFavorite: Boolean,
) : FavoriteAlbumLocalDataSource {
    val observedAlbumIds = mutableListOf<Long>()
    val added = mutableListOf<Album>()
    val removedAlbumIds = mutableListOf<Long>()

    override fun observeAll(): Flow<List<AlbumSummary>> = error("unused")

    override fun observeIsFavorite(albumId: Long): Flow<Boolean> {
        observedAlbumIds += albumId
        return flowOf(isFavorite)
    }

    override suspend fun add(album: Album) {
        added += album
    }

    override suspend fun remove(albumId: Long) {
        removedAlbumIds += albumId
    }
}
