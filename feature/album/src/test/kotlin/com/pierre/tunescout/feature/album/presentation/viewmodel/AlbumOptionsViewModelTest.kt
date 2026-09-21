package com.pierre.tunescout.feature.album.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiAction
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiEvent
import com.pierre.tunescout.ui.component.R
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AlbumOptionsViewModelTest {
    private lateinit var viewModel: AlbumOptionsViewModel
    private lateinit var enqueuer: Enqueuer
    private lateinit var navigator: Navigator
    private lateinit var actions: MutableList<AlbumOptionsUiAction>

    @Test
    fun `GIVEN a cached album WHEN adding it to the queue THEN queues every track and dismisses`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumOptionsUiEvent.OnAddToQueueClicked)

            // Then
            verify {
                enqueuer.addToQueue(album.songs)
                navigator.navigateBack()
            }
        }

    @Test
    fun `GIVEN a cached album WHEN playing it next THEN queues every track right after the current song`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10)
            prepareScenario(cached = album)

            // When
            viewModel.onEvent(AlbumOptionsUiEvent.OnPlayNextClicked)

            // Then
            verify { enqueuer.queueNext(album.songs) }
        }

    @Test
    fun `GIVEN no cached album WHEN queueing THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(cached = null)

        // When
        viewModel.onEvent(AlbumOptionsUiEvent.OnAddToQueueClicked)

        // Then
        verify(exactly = 0) { enqueuer.addToQueue(any()) }
        verify(exactly = 0) { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN only one track the player can reach WHEN queueing THEN queues that track alone`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            val album = album(id = 10, songs = listOf(song(id = 1), song(id = 2)))
            prepareScenario(cached = album, playableSongIds = setOf(2))

            // When
            viewModel.onEvent(AlbumOptionsUiEvent.OnAddToQueueClicked)

            // Then
            verify { enqueuer.addToQueue(listOf(song(id = 2))) }
        }

    @Test
    fun `GIVEN no track the player can reach WHEN queueing THEN says so and keeps the sheet open`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(cached = album(id = 10), playableSongIds = emptySet())

            // When
            viewModel.onEvent(AlbumOptionsUiEvent.OnAddToQueueClicked)

            // Then
            assertThat(actions).containsExactly(AlbumOptionsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { enqueuer.addToQueue(any()) }
            verify(exactly = 0) { navigator.navigateBack() }
        }

    private fun TestScope.prepareScenario(
        cached: Album?,
        playableSongIds: Set<Long>? = null,
    ) {
        enqueuer = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        actions = mutableListOf()
        viewModel = AlbumOptionsViewModel(
            route = AlbumOptionsRoute(albumId = 10),
            observeAlbum = { flowOf(cached) },
            enqueuer = enqueuer,
            playableSongs = PlayableSongs { song -> playableSongIds?.contains(song.id) ?: true },
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
