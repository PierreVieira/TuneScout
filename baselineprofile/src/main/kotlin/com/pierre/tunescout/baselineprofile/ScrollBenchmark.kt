package com.pierre.tunescout.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Frame timing while scrolling the two long lists: search results, which page in as they scroll,
 * and an album's tracks. Each iteration kills the app in its setup, so every one scrolls a list
 * drawn for the first time. `StartupMode.COLD` would not do: it kills the app after the setup, and
 * the measured block would find nothing on screen.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun searchResultsWithoutProfile() = searchResults(CompilationMode.None())

    @Test
    fun searchResultsWithProfile() = searchResults(CompilationMode.Partial(BaselineProfileMode.Require))

    @Test
    fun albumWithoutProfile() = album(CompilationMode.None())

    @Test
    fun albumWithProfile() = album(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun searchResults(compilationMode: CompilationMode) = measureFrames(
        compilationMode = compilationMode,
        setup = {
            killProcess()
            startAndWaitForHome()
            searchFor()
        },
        measure = { scrollSearchResults() },
    )

    private fun album(compilationMode: CompilationMode) = measureFrames(
        compilationMode = compilationMode,
        setup = {
            killProcess()
            startAndWaitForHome()
            searchFor()
            openAlbumOfFirstSearchResult()
        },
        measure = { scrollAlbum() },
    )

    private fun measureFrames(
        compilationMode: CompilationMode,
        setup: MacrobenchmarkScope.() -> Unit,
        measure: MacrobenchmarkScope.() -> Unit,
    ) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = compilationMode,
        iterations = ITERATIONS,
        setupBlock = setup,
        measureBlock = measure,
    )
}
