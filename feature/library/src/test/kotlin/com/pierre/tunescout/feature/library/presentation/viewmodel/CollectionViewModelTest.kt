package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CollectionViewModelTest {
    private lateinit var viewModel: CollectionViewModel
    private lateinit var navigator: Navigator
    private lateinit var playbackStarter: PlaybackStarter
    private lateinit var removedFavoriteIds: MutableList<Long>
    private lateinit var removedFromPlaylist: MutableList<Pair<Long, Long>>
    private lateinit var deletedPlaylistIds: MutableList<Long>

    @Test
    fun `GIVEN the favourites WHEN observing THEN titles itself with the liked songs and cannot be deleted`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

            // When
            val state = viewModel.uiState.value as CollectionUiState.Loaded

            // Then
            assertThat(state.title).isEqualTo(CollectionTitle.Favorites)
            assertThat(state.songs).hasSize(1)
            assertThat(state.isPlaying).isTrue()
            assertThat(state.isDeletable).isFalse()
        }

    @Test
    fun `GIVEN a playlist WHEN observing THEN takes its name and can be deleted`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7, name = "Road trip"),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            val state = viewModel.uiState.value as CollectionUiState.Loaded

            // Then
            assertThat(state.title).isEqualTo(CollectionTitle.Custom(name = "Road trip"))
            assertThat(state.songs.map(Song::id)).containsExactly(2L)
            assertThat(state.isDeletable).isTrue()
        }

    @Test
    fun `GIVEN a playlist that no longer exists WHEN observing THEN stays loading`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = null)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state).isEqualTo(CollectionUiState.Loading)
        }

    @Test
    fun `GIVEN a song WHEN clicking it THEN plays it alone and stays on the collection`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongClicked(song(id = 1)))

            // Then
            verify {
                playbackStarter.play(
                    song = song(id = 1),
                    songs = listOf(song(id = 1)),
                    context = PlaybackContext.SingleSong,
                )
            }
            verify(exactly = 0) { navigator.navigate(any()) }
        }

    @Test
    fun `GIVEN a song WHEN clicking its options THEN opens the sheet`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

        // When
        viewModel.onEvent(CollectionUiEvent.OnSongOptionsClicked(song(id = 1)))

        // Then
        verify { navigator.navigate(SongOptionsRoute(songId = 1)) }
    }

    @Test
    fun `GIVEN the favourites WHEN removing a song THEN unlikes it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

        // When
        viewModel.onEvent(CollectionUiEvent.OnSongRemoved(song(id = 1)))
        runCurrent()

        // Then
        assertThat(removedFavoriteIds).containsExactly(1L)
        assertThat(removedFromPlaylist).isEmpty()
    }

    @Test
    fun `GIVEN a playlist WHEN removing a song THEN takes it out of that playlist only`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                key = CollectionKey.Playlist(playlistId = 7),
                playlist = playlist(id = 7),
                playlistSongs = listOf(song(id = 2)),
            )

            // When
            viewModel.onEvent(CollectionUiEvent.OnSongRemoved(song(id = 2)))
            runCurrent()

            // Then
            assertThat(removedFromPlaylist).containsExactly(7L to 2L)
            assertThat(removedFavoriteIds).isEmpty()
        }

    @Test
    fun `GIVEN a playlist WHEN deleting it THEN removes it and goes back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))

        // When
        viewModel.onEvent(CollectionUiEvent.OnDeleteClicked)
        runCurrent()

        // Then
        assertThat(deletedPlaylistIds).containsExactly(7L)
        verify { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN the favourites WHEN deleting THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites)

        // When
        viewModel.onEvent(CollectionUiEvent.OnDeleteClicked)
        runCurrent()

        // Then
        assertThat(deletedPlaylistIds).isEmpty()
        verify(exactly = 0) { navigator.navigateBack() }
    }

    private fun TestScope.prepareScenario(
        key: CollectionKey,
        favorites: List<Song> = emptyList(),
        playlist: Playlist? = null,
        playlistSongs: List<Song> = emptyList(),
    ) {
        removedFavoriteIds = mutableListOf()
        removedFromPlaylist = mutableListOf()
        deletedPlaylistIds = mutableListOf()
        navigator = mockk(relaxUnitFun = true)
        playbackStarter = mockk(relaxUnitFun = true)
        viewModel = CollectionViewModel(
            key = key,
            useCases = CollectionUseCases(
                observePlaylist = { flowOf(playlist) },
                observePlaylistSongs = { flowOf(playlistSongs) },
                observeFavorites = { flowOf(favorites) },
                removeSongFromPlaylist = { playlistId, songId -> removedFromPlaylist += playlistId to songId },
                removeFavorite = { songId -> removedFavoriteIds += songId },
                deletePlaylist = { playlistId -> deletedPlaylistIds += playlistId },
            ),
            observablePlayback = ObservablePlayback { MutableStateFlow(playbackState()) },
            playbackStarter = playbackStarter,
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
