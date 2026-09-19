package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.feature.library.presentation.model.CreatePlaylistUiEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CreatePlaylistViewModelTest {
    private lateinit var viewModel: CreatePlaylistViewModel
    private lateinit var navigator: Navigator
    private lateinit var createdNames: MutableList<String>

    @Test
    fun `GIVEN a typed name WHEN confirming THEN creates the playlist and opens it in place of the dialog`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            viewModel.onEvent(CreatePlaylistUiEvent.OnNameChanged("  Road trip  "))

            // When
            viewModel.onEvent(CreatePlaylistUiEvent.OnConfirmClicked)
            runCurrent()

            // Then
            assertThat(createdNames).containsExactly("Road trip")
            verify { navigator.navigateReplacingTop(PlaylistRoute(playlistId = 42)) }
        }

    @Test
    fun `GIVEN a blank name WHEN confirming THEN creates nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        viewModel.onEvent(CreatePlaylistUiEvent.OnNameChanged("   "))

        // When
        viewModel.onEvent(CreatePlaylistUiEvent.OnConfirmClicked)
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.canConfirm).isFalse()
        assertThat(createdNames).isEmpty()
    }

    @Test
    fun `WHEN dismissing THEN navigates back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        viewModel.onEvent(CreatePlaylistUiEvent.OnNameChanged("Road trip"))

        // When
        viewModel.onEvent(CreatePlaylistUiEvent.OnDismissed)

        // Then
        verify { navigator.navigateBack() }
    }

    @BeforeEach
    fun setUp() {
        createdNames = mutableListOf()
        navigator = mockk(relaxUnitFun = true)
        viewModel = CreatePlaylistViewModel(
            createPlaylist = { name ->
                createdNames += name
                42L
            },
            navigator = navigator,
        )
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
