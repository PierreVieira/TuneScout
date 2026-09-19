package com.pierre.tunescout.feature.album.presentation.viewmodel

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiEvent
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

    private fun TestScope.prepareScenario(cached: Album?) {
        enqueuer = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = AlbumOptionsViewModel(
            route = AlbumOptionsRoute(albumId = 10),
            observeAlbum = { flowOf(cached) },
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
