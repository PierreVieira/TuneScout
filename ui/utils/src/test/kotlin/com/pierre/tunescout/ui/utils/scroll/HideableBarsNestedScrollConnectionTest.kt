package com.pierre.tunescout.ui.utils.scroll

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class HideableBarsNestedScrollConnectionTest {
    private val toggleDistance = 10f
    private lateinit var state: HideableBarsState
    private lateinit var connection: HideableBarsNestedScrollConnection

    @Test
    fun `GIVEN visible bars WHEN the list moves up past the toggle distance THEN hides them`() {
        // Given
        prepareScenario()

        // When
        connection.scrollContent(-11f)

        // Then
        assertThat(state.areBarsVisible).isFalse()
    }

    @Test
    fun `GIVEN visible bars WHEN the list moves up less than the toggle distance THEN keeps them`() {
        // Given
        prepareScenario()

        // When
        connection.scrollContent(-9f)

        // Then
        assertThat(state.areBarsVisible).isTrue()
    }

    @Test
    fun `GIVEN visible bars WHEN short moves up add up past the toggle distance THEN hides them`() {
        // Given
        prepareScenario()

        // When
        repeat(times = 3) { connection.scrollContent(-4f) }

        // Then
        assertThat(state.areBarsVisible).isFalse()
    }

    @Test
    fun `GIVEN visible bars WHEN the list has nothing to consume upwards THEN keeps them`() {
        // Given
        prepareScenario()

        // When
        connection.dragBy(-100f)

        // Then
        assertThat(state.areBarsVisible).isTrue()
    }

    @Test
    fun `GIVEN hidden bars WHEN dragging down past the toggle distance THEN shows them`() {
        // Given
        prepareScenario(areBarsVisible = false)

        // When
        connection.dragBy(11f)

        // Then
        assertThat(state.areBarsVisible).isTrue()
    }

    @Test
    fun `GIVEN hidden bars and a list at its top WHEN dragging down THEN still shows them`() {
        // Given
        prepareScenario(areBarsVisible = false)

        // When
        connection.onPreScroll(available = Offset(x = 0f, y = 11f), source = NestedScrollSource.UserInput)
        connection.onPostScroll(
            consumed = Offset.Zero,
            available = Offset(x = 0f, y = 11f),
            source = NestedScrollSource.UserInput,
        )

        // Then
        assertThat(state.areBarsVisible).isTrue()
    }

    @Test
    fun `GIVEN a move up short of the toggle distance WHEN it turns around THEN starts counting again`() {
        // Given
        prepareScenario()
        connection.scrollContent(-9f)

        // When
        connection.dragBy(9f)

        // Then
        assertThat(state.areBarsVisible).isTrue()
    }

    @Test
    fun `GIVEN visible bars WHEN scrolling THEN consumes nothing itself`() {
        // Given
        prepareScenario()

        // When
        val consumed = connection.scrollContent(-11f)

        // Then
        assertThat(consumed).isEqualTo(Offset.Zero)
    }

    private fun HideableBarsNestedScrollConnection.dragBy(delta: Float): Offset =
        onPreScroll(available = Offset(x = 0f, y = delta), source = NestedScrollSource.UserInput)

    private fun HideableBarsNestedScrollConnection.scrollContent(delta: Float): Offset {
        dragBy(delta)
        return onPostScroll(
            consumed = Offset(x = 0f, y = delta),
            available = Offset.Zero,
            source = NestedScrollSource.UserInput,
        )
    }

    private fun prepareScenario(areBarsVisible: Boolean = true) {
        state = HideableBarsState()
        if (!areBarsVisible) {
            state.hide()
        }
        connection = HideableBarsNestedScrollConnection(state = state, toggleDistance = toggleDistance)
    }
}
