package com.pierre.tunescout.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates `app/src/main/generated/baselineProfiles/baseline-prof.txt` and the startup profile
 * next to it from both tests. Run it with `./gradlew :app:generateBaselineProfile` and commit the
 * result. See docs/performance.md.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * Only the cold start goes into the startup profile: R8 puts what it lists in the primary dex
     * file, and code a later screen needs would push out code the first frame needs.
     */
    @Test
    fun startup() = rule.collect(packageName = TARGET_PACKAGE, includeInStartupProfile = true) {
        startAndWaitForHome()
    }

    @Test
    fun criticalJourneys() = rule.collect(packageName = TARGET_PACKAGE) {
        startAndWaitForHome()
        searchFor()
        scrollSearchResults()
        playFirstSearchResult()
        openPlayerFromMiniPlayer()
        closePlayer()
        openAlbumOfFirstSearchResult()
        scrollAlbum()
    }
}
