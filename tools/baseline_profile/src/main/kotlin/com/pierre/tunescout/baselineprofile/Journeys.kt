package com.pierre.tunescout.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/**
 * The critical user journeys, shared by the profile generator and the benchmarks so both exercise
 * the same code. The app has no test hooks for them: screens are found by what they show and by
 * the two lists tagged for this module (their tags are exposed as resource ids by `MainContent`).
 * The strings are the app's English ones, so the device has to run in English.
 */
internal const val TARGET_PACKAGE = "com.pierre.tunescout"

/** A term with enough results to page, and whose first result has an album to open. */
internal const val SEARCH_TERM = "daft punk"

/** Enough runs for stable percentiles without keeping the device busy for an hour. */
internal const val ITERATIONS = 10

private const val SEARCH_RESULTS_TAG = "search_results"
private const val ALBUM_TRACKS_TAG = "album_tracks"
private const val MORE_OPTIONS = "More options"
private const val OPEN_THE_QUEUE = "Open the queue"
private const val PLAYBACK_POSITION = "Playback position"
private const val VIEW_ALBUM = "View album"
private const val TIMEOUT_MILLIS = 15_000L
private const val PLAYER_SETTLE_MILLIS = 2_000L
private const val FLINGS = 3
private val miniPlayer: BySelector
    get() = By.clickable(true).hasDescendant(By.desc(OPEN_THE_QUEUE)).hasDescendant(By.clickable(true))

/**
 * Starts the app and waits past the splash for the search field of the home screen. The
 * notification permission is granted first, so its dialog never covers what a journey taps.
 */
internal fun MacrobenchmarkScope.startAndWaitForHome() {
    device.executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
    pressHome()
    startActivityAndWait()
    device.findWhenShown(By.clazz("android.widget.EditText"))
}

/**
 * Starts the app with none of the data a previous run left behind — no restored session, no
 * history — and waits for the home screen. The compiled code the benchmark installed stays.
 */
internal fun MacrobenchmarkScope.clearDataAndWaitForHome() {
    device.executeShellCommand("pm clear $packageName")
    startAndWaitForHome()
}

/** Types [term] into the search field and waits for the first page of results. */
internal fun MacrobenchmarkScope.searchFor(term: String = SEARCH_TERM) {
    val field = device.findWhenShown(By.clazz("android.widget.EditText"))
    field.text = term
    device.pressEnter()
    device.findWhenShown(By.res(SEARCH_RESULTS_TAG).hasDescendant(By.desc(MORE_OPTIONS)))
}

/** Flings the search results down, loading more pages on the way, and back up. */
internal fun MacrobenchmarkScope.scrollSearchResults() {
    device.findWhenShown(By.res(SEARCH_RESULTS_TAG)).flingDownAndUp(device)
}

/**
 * Plays the first search result and waits for the mini player to rise under the list. Only the
 * mini player is waited for, not the audio, so a slow network does not fail the journey.
 *
 * Without [clearDataAndWaitForHome], a cold start restores the session a previous run left
 * behind, so the song may already be the current one — and tapping the current song opens the
 * player instead, which this then leaves again.
 */
internal fun MacrobenchmarkScope.playFirstSearchResult() {
    firstRowOf(SEARCH_RESULTS_TAG).click()
    device.waitForIdle()
    if (device.wait(Until.hasObject(By.desc(PLAYBACK_POSITION)), PLAYER_SETTLE_MILLIS)) {
        closePlayer()
    }
    device.findWhenShown(miniPlayer)
}

/**
 * Opens the player from the mini player: the shared artwork transition. The mini player is the one
 * place a song is drawn beside the queue action, and the whole bar is what opens the player — the
 * clickable holding the queue button, not the button itself.
 */
internal fun MacrobenchmarkScope.openPlayerFromMiniPlayer() {
    device.findWhenShown(miniPlayer).click()
    device.findWhenShown(By.desc(PLAYBACK_POSITION))
}

/** Leaves the player the way it came in, back to the mini player. */
internal fun MacrobenchmarkScope.closePlayer() {
    device.pressBack()
    device.wait(Until.gone(By.desc(PLAYBACK_POSITION)), TIMEOUT_MILLIS)
    device.findWhenShown(miniPlayer)
}

/**
 * Opens the album of the first search result through its options sheet, whose "View album" stays
 * disabled until the song's album is known.
 */
internal fun MacrobenchmarkScope.openAlbumOfFirstSearchResult() {
    firstRowOf(SEARCH_RESULTS_TAG).findObject(By.desc(MORE_OPTIONS)).click()
    device.findWhenShown(By.clickable(true).enabled(true).hasDescendant(By.text(VIEW_ALBUM))).click()
    device.findWhenShown(By.res(ALBUM_TRACKS_TAG).hasDescendant(By.desc(MORE_OPTIONS)))
}

/** Flings the album's tracks down and back up. */
internal fun MacrobenchmarkScope.scrollAlbum() {
    device.findWhenShown(By.res(ALBUM_TRACKS_TAG)).flingDownAndUp(device)
}

/**
 * @return the first song row of the list tagged [listTag] drawn whole — a row is the clickable that
 * carries the row's options action. One clipped at the top would take a tap meant for its middle
 * somewhere else, or nowhere.
 */
private fun MacrobenchmarkScope.firstRowOf(listTag: String): UiObject2 {
    val list = device.findWhenShown(By.res(listTag))
    val rowSelector = By.clickable(true).hasDescendant(By.desc(MORE_OPTIONS))
    list.wait(Until.hasObject(rowSelector), TIMEOUT_MILLIS)
    val rows = list.findObjects(rowSelector)
    val wholeRowHeight = rows.maxOfOrNull { row -> row.visibleBounds.height() } ?: error("No song row in $listTag")
    return rows.first { row -> row.visibleBounds.height() == wholeRowHeight }
}

/** Flings this list down and back up, starting away from the edges so no gesture goes back. */
private fun UiObject2.flingDownAndUp(device: UiDevice) {
    setGestureMargin(device.displayWidth / 5)
    repeat(FLINGS) {
        fling(Direction.DOWN)
        device.waitForIdle()
    }
    repeat(FLINGS) {
        fling(Direction.UP)
        device.waitForIdle()
    }
}

/** @return what matches [selector], once it is on screen. */
private fun UiDevice.findWhenShown(selector: BySelector): UiObject2 =
    wait(Until.findObject(selector), TIMEOUT_MILLIS) ?: error("Nothing on screen matched $selector")
