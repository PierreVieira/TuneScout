package com.pierre.tunescout.ui.utils.animation

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Transition
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class SharedArtworkDestinationTest {
    private lateinit var destination: SharedArtworkDestination

    @Test
    fun `GIVEN no player on screen WHEN asked THEN it is not staying`() {
        // Given
        prepareScenario(targetState = null)

        // When
        val isStaying = destination.isStaying

        // Then
        assertThat(isStaying).isFalse()
    }

    @Test
    fun `GIVEN a player the NavDisplay is heading to WHEN asked THEN it is staying`() {
        // Given
        prepareScenario(targetState = EnterExitState.Visible)

        // When
        val isStaying = destination.isStaying

        // Then
        assertThat(isStaying).isTrue()
    }

    @Test
    fun `GIVEN a player the NavDisplay is taking away WHEN asked THEN it is not staying`() {
        // Given
        prepareScenario(targetState = EnterExitState.PostExit)

        // When
        val isStaying = destination.isStaying

        // Then
        assertThat(isStaying).isFalse()
    }

    private fun prepareScenario(targetState: EnterExitState?) {
        destination = SharedArtworkDestination()
        destination.transition = targetState?.let { state ->
            mockk<Transition<EnterExitState>> { every { this@mockk.targetState } returns state }
        }
    }
}
