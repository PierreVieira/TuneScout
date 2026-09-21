package com.pierre.tunescout

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.ComposeContext
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * An album opened from the songs: beside them on a window wide enough for two panes, over them
 * otherwise. The flows run in portrait and in landscape, and the emulator's landscape is wide enough,
 * so both halves run — which one is decided by the window, not by the test.
 */
@OptIn(ExperimentalTestApi::class)
class ListDetailFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { ListDetailSongSearchRemoteDataSource() }
        single<AlbumRemoteDataSource> { ListDetailAlbumRemoteDataSource() }
    }

    /** The width Material starts calling expanded, which is where the app lays out two panes. */
    private val isTwoPane: Boolean
        get() = InstrumentationRegistry
            .getInstrumentation()
            .targetContext.resources.configuration.screenWidthDp >= EXPANDED_WIDTH_DP

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
    }

    @AfterEach
    fun tearDown() {
        unloadKoinModules(fakeRemoteModule)
    }

    @Test
    fun anAlbumOpensBesideTheSongsOnlyOnAWideWindowAndBackClosesIt() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        searchFor("daft")
        openTheAlbumOfTheFirstResult()

        if (isTwoPane) {
            onNode(hasSetTextAction()).assertIsDisplayed()
        } else {
            assertThat(onAllNodes(hasSetTextAction()).fetchSemanticsNodes()).isEmpty()
        }

        pressBack()

        waitUntilDoesNotExist(hasText(ALBUM_ONLY_TRACK), SCREEN_TIMEOUT_MILLIS)
        onNode(hasSetTextAction()).assertIsDisplayed()
    }

    /**
     * The tab host has a back of its own — from the library back to the songs — and it must not take
     * Back from an album open beside it.
     */
    @Test
    fun backClosesTheAlbumBesideTheLibraryBeforeLeavingTheTab() = compose.use {
        if (!isTwoPane) return@use
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        searchFor("daft")
        openTheAlbumOfTheFirstResult()

        onNodeWithText("Library").performClick()
        waitUntilAtLeastOneExists(hasText("Your Library"), SCREEN_TIMEOUT_MILLIS)
        pressBack()

        waitUntilDoesNotExist(hasText(ALBUM_ONLY_TRACK), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Your Library").assertIsDisplayed()

        pressBack()

        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
    }

    /**
     * A landscape window leaves no room for results under the keyboard, so once [term] is typed this
     * closes it the way the search key does.
     */
    private fun ComposeContext.searchFor(term: String) {
        onNode(hasSetTextAction()).performTextInput(term)
        onNode(hasSetTextAction()).performImeAction()
    }

    /** A landscape window cuts the options sheet short of its last options, so it is scrolled first. */
    private fun ComposeContext.openTheAlbumOfTheFirstResult() {
        waitUntilAtLeastOneExists(hasText("Get Lucky"), SCREEN_TIMEOUT_MILLIS)
        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("View album") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("View album").performScrollTo().performClick()
        waitUntilAtLeastOneExists(hasText(ALBUM_ONLY_TRACK), SCREEN_TIMEOUT_MILLIS)
    }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val EXPANDED_WIDTH_DP = 840
        const val ALBUM_ONLY_TRACK = "Give Life Back to Music"
    }
}

private class ListDetailSongSearchRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = listOf(song(id = 1, title = "Get Lucky")).take(limit)
}

private class ListDetailAlbumRemoteDataSource : AlbumRemoteDataSource {
    override suspend fun fetchAlbum(albumId: Long): Album? = album(
        id = albumId,
        songs = listOf(
            song(id = 10, albumId = albumId, title = "Give Life Back to Music", trackNumber = 1),
            song(id = 1, albumId = albumId, title = "Get Lucky", trackNumber = 8),
        ),
    )
}
