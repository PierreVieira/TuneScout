package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.FavoriteButtonState
import com.pierre.tunescout.core.playback.internal.PlaybackFavoriteController
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PlaybackFavoriteControllerTest {
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var favoriteSongLocalDataSource: FakeFavoriteSongLocalDataSource
    private lateinit var controller: PlaybackFavoriteController
    private lateinit var observedStates: MutableList<FavoriteButtonState?>

    @Test
    fun `GIVEN no song is loaded WHEN observing THEN emits nothing playing`() = runTest {
        // Given
        prepareScenario()

        // When
        runCurrent()

        // Then
        assertThat(observedStates).containsExactly(null)
    }

    @Test
    fun `GIVEN a song that is not liked WHEN it starts playing THEN emits it as not favorite`() = runTest {
        // Given
        prepareScenario()
        val currentSong = song(id = 1)

        // When
        playbackStateFlow.value = playbackState(songs = listOf(currentSong))
        runCurrent()

        // Then
        assertThat(observedStates.last()).isEqualTo(FavoriteButtonState(currentSong, isFavorite = false))
    }

    @Test
    fun `GIVEN a song that is liked WHEN it starts playing THEN emits it as favorite`() = runTest {
        // Given
        prepareScenario()
        val currentSong = song(id = 1)
        favoriteSongLocalDataSource.setFavorite(currentSong.id, isFavorite = true)

        // When
        playbackStateFlow.value = playbackState(songs = listOf(currentSong))
        runCurrent()

        // Then
        assertThat(observedStates.last()).isEqualTo(FavoriteButtonState(currentSong, isFavorite = true))
    }

    @Test
    fun `GIVEN the queue advances WHEN the next song plays THEN switches to its favorite state`() = runTest {
        // Given
        prepareScenario()
        val first = song(id = 1)
        val second = song(id = 2)
        favoriteSongLocalDataSource.setFavorite(second.id, isFavorite = true)

        // When
        playbackStateFlow.value = playbackState(songs = listOf(first))
        runCurrent()
        playbackStateFlow.value = playbackState(songs = listOf(second))
        runCurrent()

        // Then
        assertThat(observedStates)
            .containsExactly(
                FavoriteButtonState(first, isFavorite = false),
                FavoriteButtonState(second, isFavorite = true),
            ).inOrder()
    }

    @Test
    fun `GIVEN a song is playing WHEN it is toggled THEN observing reflects the new favorite state`() = runTest {
        // Given
        prepareScenario()
        val currentSong = song(id = 1)
        playbackStateFlow.value = playbackState(songs = listOf(currentSong))
        runCurrent()

        // When
        controller.toggle(FavoriteButtonState(currentSong, isFavorite = false))
        runCurrent()

        // Then
        assertThat(observedStates.last()).isEqualTo(FavoriteButtonState(currentSong, isFavorite = true))
    }

    @Test
    fun `GIVEN a song that is not liked WHEN toggling it THEN it is saved`() = runTest {
        // Given
        prepareScenario()
        val currentSong = song(id = 1)

        // When
        controller.toggle(FavoriteButtonState(currentSong, isFavorite = false))

        // Then
        assertThat(favoriteSongLocalDataSource.added).containsExactly(currentSong)
        assertThat(favoriteSongLocalDataSource.removedSongIds).isEmpty()
    }

    @Test
    fun `GIVEN a liked song WHEN toggling it THEN it is dropped`() = runTest {
        // Given
        prepareScenario()
        val currentSong = song(id = 1)

        // When
        controller.toggle(FavoriteButtonState(currentSong, isFavorite = true))

        // Then
        assertThat(favoriteSongLocalDataSource.removedSongIds).containsExactly(1L)
        assertThat(favoriteSongLocalDataSource.added).isEmpty()
    }

    private fun TestScope.prepareScenario() {
        playbackStateFlow = MutableStateFlow(PlaybackState.Idle)
        favoriteSongLocalDataSource = FakeFavoriteSongLocalDataSource()
        observedStates = mutableListOf()
        controller = PlaybackFavoriteController(
            playbackState = playbackStateFlow,
            favoriteSongLocalDataSource = favoriteSongLocalDataSource,
        )
        controller
            .observe()
            .onEach { state -> observedStates += state }
            .launchIn(backgroundScope)
    }
}

private class FakeFavoriteSongLocalDataSource : FavoriteSongLocalDataSource {
    val added = mutableListOf<Song>()
    val removedSongIds = mutableListOf<Long>()
    private val favoriteFlowsBySongId = mutableMapOf<Long, MutableStateFlow<Boolean>>()

    fun setFavorite(
        songId: Long,
        isFavorite: Boolean,
    ) {
        favoriteFlowsBySongId.getOrPut(songId) { MutableStateFlow(false) }.value = isFavorite
    }

    override fun observeAll(): Flow<List<Song>> = error("unused")

    override fun observeIsFavorite(songId: Long): Flow<Boolean> =
        favoriteFlowsBySongId.getOrPut(songId) { MutableStateFlow(false) }

    override suspend fun add(song: Song) {
        added += song
        setFavorite(song.id, isFavorite = true)
    }

    override suspend fun remove(songId: Long) {
        removedSongIds += songId
        setFavorite(songId, isFavorite = false)
    }
}
