package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CollectionOptionsViewModelTest {
    private lateinit var viewModel: CollectionOptionsViewModel
    private lateinit var enqueuer: Enqueuer
    private lateinit var navigator: Navigator
    private lateinit var deletedPlaylistIds: MutableList<Long>

    @Test
    fun `GIVEN a playlist WHEN observing THEN shows its name and its songs`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(
            key = CollectionKey.Playlist(playlistId = 7),
            playlist = playlist(id = 7, name = "Road trip"),
            playlistSongs = listOf(song(id = 2)),
        )

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.title).isEqualTo(CollectionTitle.Custom(name = "Road trip"))
        assertThat(state.songs.map(Song::id)).containsExactly(2L)
        assertThat(state.isDeletable).isTrue()
    }

    @Test
    fun `GIVEN the favourites WHEN observing THEN they cannot be deleted`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites, favorites = listOf(song(id = 1)))

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.title).isEqualTo(CollectionTitle.Favorites)
        assertThat(state.isDeletable).isFalse()
    }

    @Test
    fun `GIVEN a playlist WHEN clicking delete THEN only asks for confirmation`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))

        // When
        viewModel.onEvent(CollectionOptionsUiEvent.OnDeleteClicked)
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.isConfirmingDelete).isTrue()
        assertThat(deletedPlaylistIds).isEmpty()
        verify(exactly = 0) { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN a pending delete WHEN confirming THEN it goes and both the sheet and the playlist screen close`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))
            viewModel.onEvent(CollectionOptionsUiEvent.OnDeleteClicked)

            // When
            viewModel.onEvent(CollectionOptionsUiEvent.OnDeleteConfirmed)
            runCurrent()

            // Then
            assertThat(deletedPlaylistIds).containsExactly(7L)
            verifySequence {
                navigator.navigateBack()
                navigator.navigateBack()
            }
        }

    @Test
    fun `GIVEN a pending delete WHEN dismissing it THEN keeps the playlist and the sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(key = CollectionKey.Playlist(playlistId = 7), playlist = playlist(id = 7))
            viewModel.onEvent(CollectionOptionsUiEvent.OnDeleteClicked)

            // When
            viewModel.onEvent(CollectionOptionsUiEvent.OnDeleteDismissed)
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.isConfirmingDelete).isFalse()
            assertThat(deletedPlaylistIds).isEmpty()
            verify(exactly = 0) { navigator.navigateBack() }
        }

    @Test
    fun `GIVEN the favourites WHEN deleting THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites)

        // When
        viewModel.onEvent(CollectionOptionsUiEvent.OnDeleteClicked)
        viewModel.onEvent(CollectionOptionsUiEvent.OnDeleteConfirmed)
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.isConfirmingDelete).isFalse()
        assertThat(deletedPlaylistIds).isEmpty()
        verify(exactly = 0) { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN the favourites WHEN playing them next THEN every liked song is queued and the sheet closes`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1), song(id = 2))
            prepareScenario(key = CollectionKey.Favorites, favorites = songs)

            // When
            viewModel.onEvent(CollectionOptionsUiEvent.OnPlayNextClicked)

            // Then
            verifyOrder {
                enqueuer.queueNext(songs)
                navigator.navigateBack()
            }
        }

    @Test
    fun `GIVEN the favourites WHEN adding them to the queue THEN every liked song lands behind it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val songs = listOf(song(id = 1))
            prepareScenario(key = CollectionKey.Favorites, favorites = songs)

            // When
            viewModel.onEvent(CollectionOptionsUiEvent.OnAddToQueueClicked)

            // Then
            verifyOrder {
                enqueuer.addToQueue(songs)
                navigator.navigateBack()
            }
        }

    @Test
    fun `GIVEN an empty collection WHEN queueing it THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(key = CollectionKey.Favorites)

        // When
        viewModel.onEvent(CollectionOptionsUiEvent.OnAddToQueueClicked)

        // Then
        verify(exactly = 0) { enqueuer.addToQueue(any()) }
        verify(exactly = 0) { navigator.navigateBack() }
    }

    private fun TestScope.prepareScenario(
        key: CollectionKey,
        favorites: List<Song> = emptyList(),
        playlist: Playlist? = null,
        playlistSongs: List<Song> = emptyList(),
    ) {
        deletedPlaylistIds = mutableListOf()
        enqueuer = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = CollectionOptionsViewModel(
            key = key,
            useCases = CollectionUseCases(
                observePlaylist = { flowOf(playlist) },
                observePlaylistSongs = { flowOf(playlistSongs) },
                observeFavorites = { flowOf(favorites) },
                removeSongFromPlaylist = { _, _ -> },
                removeFavorite = { },
                deletePlaylist = { playlistId -> deletedPlaylistIds += playlistId },
            ),
            enqueuer = enqueuer,
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
