package com.pierre.tunescout.ui.utils

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class ActionViewModelTest {
    private lateinit var viewModel: FakeActionViewModel
    private lateinit var actions: MutableList<String>

    @Test
    fun `GIVEN a screen collecting WHEN an action is emitted THEN the screen receives it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.emit("first")
            viewModel.emit("second")

            // Then
            assertThat(actions).containsExactly("first", "second").inOrder()
        }

    @Test
    fun `GIVEN nothing collecting WHEN an action is emitted THEN a screen collecting later misses it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            viewModel = FakeActionViewModel()
            actions = mutableListOf()

            // When
            viewModel.emit("missed")
            collectActions()

            // Then
            assertThat(actions).isEmpty()
        }

    private fun TestScope.prepareScenario() {
        viewModel = FakeActionViewModel()
        actions = mutableListOf()
        collectActions()
    }

    private fun TestScope.collectActions() {
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    private class FakeActionViewModel : ActionViewModel<String>() {
        fun emit(action: String) {
            emitAction(action)
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
