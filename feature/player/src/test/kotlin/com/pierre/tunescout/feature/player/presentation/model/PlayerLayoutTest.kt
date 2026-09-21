package com.pierre.tunescout.feature.player.presentation.model

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.ui.utils.window.TuneScoutWindowSize
import org.junit.jupiter.api.Test

class PlayerLayoutTest {
    private val phoneUpright =
        TuneScoutWindowSize(isWidthCompact = true, isWidthExpanded = false, isHeightCompact = false)
    private val phoneOnItsSide =
        TuneScoutWindowSize(isWidthCompact = false, isWidthExpanded = true, isHeightCompact = true)
    private val tabletOnItsSide =
        TuneScoutWindowSize(isWidthCompact = false, isWidthExpanded = true, isHeightCompact = false)

    @Test
    fun `GIVEN the whole window WHEN choosing THEN stacks upright and goes side by side on its side`() {
        assertThat(PlayerLayout.of(windowSize = phoneUpright, isInDetailPane = false)).isEqualTo(PlayerLayout.Stacked)
        assertThat(PlayerLayout.of(windowSize = phoneOnItsSide, isInDetailPane = false))
            .isEqualTo(PlayerLayout.SideBySide)
        assertThat(PlayerLayout.of(windowSize = tabletOnItsSide, isInDetailPane = false))
            .isEqualTo(PlayerLayout.SideBySide)
    }

    @Test
    fun `GIVEN the pane beside the tabs on a short window WHEN choosing THEN is compact`() {
        assertThat(PlayerLayout.of(windowSize = phoneOnItsSide, isInDetailPane = true)).isEqualTo(PlayerLayout.Compact)
    }

    @Test
    fun `GIVEN the pane beside the tabs on a tall window WHEN choosing THEN stacks`() {
        assertThat(PlayerLayout.of(windowSize = tabletOnItsSide, isInDetailPane = true)).isEqualTo(PlayerLayout.Stacked)
    }
}
