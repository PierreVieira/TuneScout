package com.pierre.tunescout.ui.utils.window

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TuneScoutWindowSizeTest {
    @Test
    fun `GIVEN a phone on its side with a pane beside the tabs WHEN asking THEN the tab header stacks`() {
        // Given
        val windowSize = windowSize(isHeightCompact = true, isTwoPane = true)

        // When
        val isInline = windowSize.isTabHeaderInline

        // Then
        assertThat(isInline).isFalse()
    }

    @Test
    fun `GIVEN a phone on its side with the tabs alone WHEN asking THEN the tab header is inline`() {
        // Given
        val windowSize = windowSize(isHeightCompact = true, isTwoPane = false)

        // When
        val isInline = windowSize.isTabHeaderInline

        // Then
        assertThat(isInline).isTrue()
    }

    @Test
    fun `GIVEN a tablet on its side with a pane beside the tabs WHEN asking THEN the tab header is inline`() {
        // Given
        val windowSize = windowSize(isWidthExpanded = true, isTwoPane = true)

        // When
        val isInline = windowSize.isTabHeaderInline

        // Then
        assertThat(isInline).isTrue()
    }

    @Test
    fun `GIVEN a phone upright WHEN asking THEN the tab header stacks`() {
        // Given
        val windowSize = windowSize(isWidthCompact = true)

        // When
        val isInline = windowSize.isTabHeaderInline

        // Then
        assertThat(isInline).isFalse()
    }

    private fun windowSize(
        isWidthCompact: Boolean = false,
        isWidthExpanded: Boolean = false,
        isHeightCompact: Boolean = false,
        isTwoPane: Boolean = false,
    ) = TuneScoutWindowSize(
        isWidthCompact = isWidthCompact,
        isWidthExpanded = isWidthExpanded,
        isHeightCompact = isHeightCompact,
        isTwoPane = isTwoPane,
    )
}
