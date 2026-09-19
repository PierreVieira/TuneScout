package com.pierre.tunescout.feature.addtoplaylist.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.AddToPlaylistUseCases
import com.pierre.tunescout.feature.addtoplaylist.presentation.model.AddToPlaylistUiEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AddToPlaylistViewModelTest {
    private lateinit var viewModel: AddToPlaylistViewModel
    private lateinit var navigator: Navigator
    private lateinit var addedSongs: MutableList<Pair<Long, Long>>
    private lateinit var createdWithSong: MutableList<Pair<String, Long>>

    @Test
    fun `GIVEN a cached song WHEN picking a playlist THEN adds it and dismisses`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1), playlists = listOf(playlist(id = 7)))

            // When
            viewModel.onEvent(AddToPlaylistUiEvent.OnPlaylistClicked(playlistId = 7))
            runCurrent()

            // Then
            assertThat(addedSongs).containsExactly(7L to 1L)
            verify { navigator.navigateBack() }
        }

    @Test
    fun `GIVEN no cached song WHEN picking a playlist THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = null, playlists = listOf(playlist(id = 7)))

        // When
        viewModel.onEvent(AddToPlaylistUiEvent.OnPlaylistClicked(playlistId = 7))
        runCurrent()

        // Then
        assertThat(addedSongs).isEmpty()
        verify(exactly = 0) { navigator.navigateBack() }
    }

    @Test
    fun `WHEN opening the new playlist prompt THEN the sheet reports it is open`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1))

            // When
            viewModel.onEvent(AddToPlaylistUiEvent.OnNewPlaylistClicked)
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.isPromptOpen).isTrue()
            assertThat(viewModel.uiState.value.canConfirmNewPlaylist).isFalse()
        }

    @Test
    fun `GIVEN a typed name WHEN confirming THEN creates the playlist with the song in it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1))
            viewModel.onEvent(AddToPlaylistUiEvent.OnNewPlaylistClicked)
            viewModel.onEvent(AddToPlaylistUiEvent.OnNewPlaylistNameChanged("  Road trip "))
            runCurrent()

            // When
            viewModel.onEvent(AddToPlaylistUiEvent.OnNewPlaylistConfirmed)
            runCurrent()

            // Then
            assertThat(createdWithSong).containsExactly("Road trip" to 1L)
            verify { navigator.navigateBack() }
        }

    @Test
    fun `GIVEN an open prompt WHEN dismissing it THEN the sheet stays open`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = song(id = 1))
        viewModel.onEvent(AddToPlaylistUiEvent.OnNewPlaylistClicked)
        runCurrent()

        // When
        viewModel.onEvent(AddToPlaylistUiEvent.OnNewPlaylistDismissed)
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.isPromptOpen).isFalse()
        verify(exactly = 0) { navigator.navigateBack() }
    }

    private fun TestScope.prepareScenario(
        song: Song?,
        playlists: List<Playlist> = emptyList(),
    ) {
        addedSongs = mutableListOf()
        createdWithSong = mutableListOf()
        navigator = mockk(relaxUnitFun = true)
        viewModel = AddToPlaylistViewModel(
            route = AddToPlaylistRoute(songId = 1),
            useCases = AddToPlaylistUseCases(
                observePlaylists = { flowOf(playlists) },
                observeSong = { flowOf(song) },
                addSongToPlaylist = { playlistId, added -> addedSongs += playlistId to added.id },
                createPlaylistWithSong = { name, added -> createdWithSong += name to added.id },
            ),
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
