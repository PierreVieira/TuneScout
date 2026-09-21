package com.pierre.tunescout

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.playback.ObservableDownloads
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import de.mannodermaus.junit5.compose.ComposeContext
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.Base64

private const val ALBUM_ID = 50L
private val albumTracks = listOf(
    song(
        id = 501,
        title = "Give Life Back to Music",
        albumId = ALBUM_ID,
        trackNumber = 1,
        previewUrl = DataPreview.urlFor(songId = 501),
    ),
    song(
        id = 502,
        title = "The Game of Love",
        albumId = ALBUM_ID,
        trackNumber = 2,
        previewUrl = DataPreview.urlFor(songId = 502),
    ),
)
private val searchResult = song(
    id = 601,
    title = "Digital Love",
    albumId = ALBUM_ID,
    previewUrl = DataPreview.urlFor(songId = 601),
)

@OptIn(ExperimentalTestApi::class)
class DownloadFlowTest {
    @JvmField
    @RegisterExtension
    val compose = createAndroidComposeExtension<MainActivity>()

    private val downloads: DownloadLocalDataSource
        get() = GlobalContext.get().get()

    private val observableDownloads: ObservableDownloads
        get() = GlobalContext.get().get()

    private val favorites: FavoriteSongLocalDataSource
        get() = GlobalContext.get().get()

    private val recentlyPlayed: RecentlyPlayedLocalDataSource
        get() = GlobalContext.get().get()

    private val likedBefore =
        song(id = 701, title = "One More Time", albumId = 70, previewUrl = DataPreview.urlFor(701))
    private val likedAfter = song(id = 702, title = "Aerodynamic", albumId = 70, previewUrl = DataPreview.urlFor(702))

    private val fakeRemoteModule: Module = module {
        single<SongSearchRemoteDataSource> { DownloadCatalogRemoteDataSource() }
        single<AlbumRemoteDataSource> { DownloadAlbumRemoteDataSource() }
    }

    @BeforeEach
    fun setUp() {
        loadKoinModules(fakeRemoteModule)
    }

    @AfterEach
    fun tearDown() {
        unloadKoinModules(fakeRemoteModule)
    }

    /**
     * The album's switch fetches every track through the real download manager: the switch ends up
     * on and full, and each row says its song is on the device. Turning it off takes them back off.
     */
    @Test
    fun downloadingAnAlbumPutsEveryTrackOnTheDeviceAndTurningItOffTakesThemBack() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        searchFor("daft")
        waitUntilAtLeastOneExists(hasText(searchResult.title), SCREEN_TIMEOUT_MILLIS)
        openTheAlbumOfTheFirstResult()

        onNode(downloadSwitch).performClick()
        waitUntilAtLeastOneExists(downloadSwitch and hasStateDescription("Downloaded"), DOWNLOAD_TIMEOUT_MILLIS)
        albumTracks.forEach { track ->
            waitUntilAtLeastOneExists(
                hasText(track.title, substring = true) and hasStateDescription("Downloaded"),
                DOWNLOAD_TIMEOUT_MILLIS,
            )
        }
        assertThat(storedCollections()).contains(LibraryItemKey.Album(albumId = ALBUM_ID))
        assertThat(downloadedSongIds()).containsAtLeastElementsIn(albumTracks.map(Song::id))

        onNode(downloadSwitch).performClick()
        waitUntilAtLeastOneExists(downloadSwitch and hasStateDescription("Not downloaded"), SCREEN_TIMEOUT_MILLIS)
        waitUntil(DOWNLOAD_TIMEOUT_MILLIS) {
            downloadedSongIds().none { songId ->
                songId in albumTracks.map(Song::id)
            }
        }
    }

    @Test
    fun aSongDownloadedFromItsOptionsSheetIsMarkedDownloadedInTheResults() = compose.use {
        waitUntilAtLeastOneExists(hasSetTextAction(), SCREEN_TIMEOUT_MILLIS)
        searchFor("daft")
        waitUntilAtLeastOneExists(hasText(searchResult.title), SCREEN_TIMEOUT_MILLIS)

        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("Download") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("Download").performScrollTo().performClick()

        waitUntilAtLeastOneExists(
            hasText(searchResult.title, substring = true) and hasStateDescription("Downloaded"),
            DOWNLOAD_TIMEOUT_MILLIS,
        )
        assertThat(downloadedSongIds()).contains(searchResult.id)
    }

    /**
     * The liked songs are downloaded before the test starts; a song liked afterwards is downloaded
     * with them, without anyone asking for it on its own.
     */
    @Test
    fun aSongLikedAfterTheLikedSongsWereDownloadedIsDownloadedWithThem() {
        runBlocking {
            favorites.add(likedBefore)
            downloads.addCollection(LibraryItemKey.Favorites)
            recentlyPlayed.record(likedAfter)
        }
        compose.use {
            waitUntilAtLeastOneExists(hasText(likedAfter.title), SCREEN_TIMEOUT_MILLIS)
            waitUntil(DOWNLOAD_TIMEOUT_MILLIS) { likedBefore.id in downloadedSongIds() }

            onAllNodesWithContentDescription("More options")[0].performClick()
            waitUntilAtLeastOneExists(hasText("Like") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
            onNodeWithText("Like").performClick()

            waitUntilAtLeastOneExists(
                hasText(likedAfter.title, substring = true) and hasStateDescription("Downloaded"),
                DOWNLOAD_TIMEOUT_MILLIS,
            )
            assertThat(downloadedSongIds()).containsAtLeast(likedBefore.id, likedAfter.id)
        }
    }

    private val downloadSwitch: SemanticsMatcher = hasContentDescription("Download")

    private fun hasStateDescription(state: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, state)

    private fun downloadedSongIds(): Set<Long> = runBlocking {
        observableDownloads
            .observeDownloadStatuses()
            .first()
            .filterValues { status -> status == SongDownloadStatus.Downloaded }
            .keys
    }

    private fun storedCollections(): Set<LibraryItemKey> = runBlocking { downloads.observeCollections().first() }

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
        onAllNodesWithContentDescription("More options")[0].performClick()
        waitUntilAtLeastOneExists(hasText("View album") and isEnabled(), SCREEN_TIMEOUT_MILLIS)
        onNodeWithText("View album").performScrollTo().performClick()
        waitUntilAtLeastOneExists(downloadSwitch, SCREEN_TIMEOUT_MILLIS)
    }

    private companion object {
        const val SCREEN_TIMEOUT_MILLIS = 10_000L
        const val DOWNLOAD_TIMEOUT_MILLIS = 20_000L
    }
}

private class DownloadCatalogRemoteDataSource : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = listOf(searchResult)
}

private class DownloadAlbumRemoteDataSource : AlbumRemoteDataSource {
    override suspend fun fetchAlbum(albumId: Long): Album? = album(id = albumId, songs = albumTracks)
}

/**
 * A preview the device can fetch with no server behind it: a `data:` url carries its own bytes, so
 * the real download manager, cache and service run end to end and the test still decides what
 * arrives. Each song gets its own bytes, so each one is its own entry in the cache.
 */
private object DataPreview {
    private const val PREVIEW_BYTES = 2048

    fun urlFor(songId: Long): String {
        val bytes = ByteArray(PREVIEW_BYTES) { index -> (index + songId).toByte() }
        return "data:audio/mp4;base64,${Base64.getEncoder().encodeToString(bytes)}"
    }
}
