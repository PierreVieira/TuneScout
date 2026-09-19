package com.pierre.tunescout.navigation.home

import androidx.compose.runtime.mutableIntStateOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class HomeTabsStateTest {
    @Test
    fun `WHEN the host opens THEN the first tab is the one on screen`() {
        // When
        val state = prepareScenario()

        // Then
        assertThat(state.selectedTab).isEqualTo(HomeTab.SONGS)
    }

    @Test
    fun `GIVEN the first tab WHEN switching to another THEN that one is on screen`() {
        // Given
        val state = prepareScenario()

        // When
        state.switchTo(HomeTab.LIBRARY)

        // Then
        assertThat(state.selectedTab).isEqualTo(HomeTab.LIBRARY)
    }

    @Test
    fun `GIVEN a tab other than the first WHEN going back THEN the first tab comes back`() {
        // Given
        val state = prepareScenario()
        state.switchTo(HomeTab.LIBRARY)

        // When
        state.navigateBack()

        // Then
        assertThat(state.selectedTab).isEqualTo(HomeTab.SONGS)
    }

    @Test
    fun `GIVEN the first tab WHEN going back THEN it stays on screen`() {
        // Given
        val state = prepareScenario()

        // When
        state.navigateBack()

        // Then
        assertThat(state.selectedTab).isEqualTo(HomeTab.SONGS)
    }

    @Test
    fun `GIVEN a selected index saved from an earlier process WHEN the host opens THEN it is restored`() {
        // When
        val state = prepareScenario(selectedIndex = HomeTab.LIBRARY.ordinal)

        // Then
        assertThat(state.selectedTab).isEqualTo(HomeTab.LIBRARY)
    }

    /**
     * The back stacks are only read by `toDecoratedEntries`, which is a composable and belongs to
     * the instrumented suites, so the state under test here needs none of them.
     */
    private fun prepareScenario(selectedIndex: Int = 0): HomeTabsState = HomeTabsState(
        backStacks = emptyMap(),
        selectedIndexState = mutableIntStateOf(selectedIndex),
    )
}
