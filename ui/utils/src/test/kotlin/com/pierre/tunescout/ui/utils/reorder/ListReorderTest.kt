package com.pierre.tunescout.ui.utils.reorder

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ListReorderTest {
    private lateinit var reorder: ListReorder<String, String>
    private lateinit var stored: MutableList<List<String>>
    private lateinit var items: MutableStateFlow<List<String>>

    @Test
    fun `GIVEN a list never reordered WHEN observing it THEN keeps its own order`() = runTest {
        // Given
        prepareScenario()

        // When
        val arranged = reorder.observeArranged(items).first()

        // Then
        assertThat(arranged).containsExactly("a", "b", "c").inOrder()
        assertThat(stored).isEmpty()
    }

    @Test
    fun `GIVEN a list WHEN moving an item down onto another THEN it takes that place and is stored`() = runTest {
        // Given
        prepareScenario()

        // When
        reorder.move(items = items.value, from = "a", to = "c")
        runCurrent()

        // Then
        assertThat(reorder.observeArranged(items).first()).containsExactly("b", "c", "a").inOrder()
        assertThat(stored).containsExactly(listOf("b", "c", "a"))
    }

    @Test
    fun `GIVEN a list WHEN moving an item up onto another THEN it takes that place`() = runTest {
        // Given
        prepareScenario()

        // When
        reorder.move(items = items.value, from = "c", to = "a")
        runCurrent()

        // Then
        assertThat(reorder.observeArranged(items).first()).containsExactly("c", "a", "b").inOrder()
    }

    @Test
    fun `GIVEN a key the list does not hold WHEN moving it THEN nothing changes`() = runTest {
        // Given
        prepareScenario()

        // When
        reorder.move(items = items.value, from = "z", to = "a")
        reorder.move(items = items.value, from = "a", to = "a")
        runCurrent()

        // Then
        assertThat(reorder.observeArranged(items).first()).containsExactly("a", "b", "c").inOrder()
        assertThat(stored).isEmpty()
    }

    @Test
    fun `GIVEN a reordered list WHEN an item is added THEN it goes after the ones already ordered`() = runTest {
        // Given
        prepareScenario()
        reorder.move(items = items.value, from = "c", to = "a")

        // When
        items.value = listOf("a", "b", "c", "d")

        // Then
        assertThat(reorder.observeArranged(items).first()).containsExactly("c", "a", "b", "d").inOrder()
    }

    @Test
    fun `GIVEN a list WHEN starting and finishing THEN the mode follows`() = runTest {
        // Given
        prepareScenario()

        // When
        reorder.start()
        val whileReordering = reorder.isReordering.value
        reorder.finish()

        // Then
        assertThat(whileReordering).isTrue()
        assertThat(reorder.isReordering.value).isFalse()
    }

    private fun TestScope.prepareScenario() {
        stored = mutableListOf()
        items = MutableStateFlow(listOf("a", "b", "c"))
        reorder = ListReorder(keyOf = { item -> item }, scope = backgroundScope, persist = { keys -> stored += keys })
        runCurrent()
    }
}
