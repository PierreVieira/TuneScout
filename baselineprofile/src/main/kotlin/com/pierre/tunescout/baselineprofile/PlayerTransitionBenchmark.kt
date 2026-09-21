package com.pierre.tunescout.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame timing while the player opens from the mini player — the shared artwork transition — and
 * closes back to it. Each iteration clears the app's data in its setup, so the song it plays is
 * never the current one already, which would open the player straight from the list.
 */
@RunWith(AndroidJUnit4::class)
class PlayerTransitionBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun openPlayerWithoutProfile() = openPlayer(CompilationMode.None())

    @Test
    fun openPlayerWithProfile() = openPlayer(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun openPlayer(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        iterations = ITERATIONS,
        setupBlock = {
            clearDataAndWaitForHome()
            searchFor()
            playFirstSearchResult()
        },
    ) {
        openPlayerFromMiniPlayer()
        closePlayer()
    }
}
