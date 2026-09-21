package com.pierre.tunescout.screenshottests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pierre.tunescout.screenshotfixtures.getLucky
import com.pierre.tunescout.ui.component.CollectionDownloadButton
import com.pierre.tunescout.ui.component.ConfirmationDialog
import com.pierre.tunescout.ui.component.DownloadIndicator
import com.pierre.tunescout.ui.component.NamePromptCard
import com.pierre.tunescout.ui.component.NoticeBar
import com.pierre.tunescout.ui.component.NowPlayingState
import com.pierre.tunescout.ui.component.PlayButtonState
import com.pierre.tunescout.ui.component.PlayPauseButton
import com.pierre.tunescout.ui.component.SearchField
import com.pierre.tunescout.ui.component.SeekBar
import com.pierre.tunescout.ui.component.SongListSkeleton
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowMoreAction
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBar
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import org.junit.Test

/**
 * The pieces every screen is built from, captured on their own: a change here shows up as one diff
 * instead of the same diff repeated across every screen that draws them.
 */
internal class ComponentScreenshotTest : ScreenshotTest() {
    @Test
    fun songRows() {
        snapshot(name = "song_rows") {
            GalleryContent {
                SongRow(
                    title = getLucky.title,
                    subtitle = getLucky.artistName,
                    artworkUrl = getLucky.artwork.thumbnailUrl,
                    onClick = {},
                )
                SongRow(
                    title = getLucky.title,
                    subtitle = getLucky.artistName,
                    artworkUrl = getLucky.artwork.thumbnailUrl,
                    onClick = {},
                    nowPlaying = NowPlayingState.Playing,
                    trailing = { SongRowMoreAction(onClick = {}) },
                )
                SongRow(
                    title = getLucky.title,
                    subtitle = getLucky.artistName,
                    artworkUrl = getLucky.artwork.thumbnailUrl,
                    onClick = {},
                    nowPlaying = NowPlayingState.Paused,
                )
                SongListSkeleton(rows = 2, hasMoreAction = true)
            }
        }
    }

    /** A song on its way, one on the device, and one that cannot play offline, which shows only why. */
    @Test
    fun songRowDownloads() {
        snapshot(name = "song_row_downloads", variants = ScreenshotVariant.all) {
            GalleryContent {
                SongRow(
                    title = getLucky.title,
                    subtitle = getLucky.artistName,
                    artworkUrl = getLucky.artwork.thumbnailUrl,
                    onClick = {},
                    downloadIndicator = DownloadIndicator.Downloading,
                )
                SongRow(
                    title = getLucky.title,
                    subtitle = getLucky.artistName,
                    artworkUrl = getLucky.artwork.thumbnailUrl,
                    onClick = {},
                    downloadIndicator = DownloadIndicator.Downloaded,
                    trailing = { SongRowMoreAction(onClick = {}) },
                )
                SongRow(
                    title = getLucky.title,
                    subtitle = getLucky.artistName,
                    artworkUrl = getLucky.artwork.thumbnailUrl,
                    onClick = {},
                    isUnavailable = true,
                    downloadIndicator = DownloadIndicator.Downloading,
                )
            }
        }
    }

    /** Not downloaded, a third of the way there, and every song on the device. */
    @Test
    fun collectionDownloadButtonStates() {
        snapshot(name = "collection_download_button_states", variants = ScreenshotVariant.all) {
            GalleryContent {
                listOf(null, 1f / 3, 1f).forEach { progress ->
                    CollectionDownloadButton(progress = progress, totalCount = 3, onClick = {})
                }
            }
        }
    }

    @Test
    fun barsAndInputs() {
        snapshot(name = "bars_and_inputs", variants = ScreenshotVariant.all) {
            GalleryContent {
                TopBar(title = "Random Access Memories", onBackClick = {})
                SearchField(query = "", placeholder = "Search songs", onQueryChange = {}, onClear = {})
                SearchField(query = "daft punk", placeholder = "Search songs", onQueryChange = {}, onClear = {})
                NoticeBar(text = "You are offline. Showing what is on the device.")
                SeekBar(progress = 0.62f, contentKey = getLucky.id, onSeekFinished = {})
            }
        }
    }

    @Test
    fun playButtonStates() {
        snapshot(name = "play_button_states") {
            GalleryContent {
                PlayButtonState.entries.forEach { state ->
                    PlayPauseButton(state = state, onClick = {})
                }
            }
        }
    }

    @Test
    fun stateMessages() {
        snapshot(name = "state_messages", variants = ScreenshotVariant.all) {
            GalleryContent {
                StateMessage(title = "Nothing here yet")
                StateMessage(
                    title = "Could not load the album",
                    description = "Check your connection and try again.",
                    onRetry = {},
                )
            }
        }
    }

    @Test
    fun namePrompt() {
        snapshot(name = "name_prompt") {
            GalleryContent {
                NamePromptCard(
                    title = "New playlist",
                    placeholder = "Playlist name",
                    confirmLabel = "Create",
                    cancelLabel = "Cancel",
                    name = "",
                    canConfirm = false,
                    onNameChange = {},
                    onConfirm = {},
                    onCancel = {},
                )
                NamePromptCard(
                    title = "New playlist",
                    placeholder = "Playlist name",
                    confirmLabel = "Create",
                    cancelLabel = "Cancel",
                    name = "Late night drive",
                    canConfirm = true,
                    onNameChange = {},
                    onConfirm = {},
                    onCancel = {},
                )
            }
        }
    }

    @Test
    fun confirmationDialog() {
        snapshot(name = "confirmation_dialog", variants = ScreenshotVariant.all) {
            ConfirmationDialog(
                title = "Remove from recently played?",
                message = "Get Lucky will be removed from the list.",
                confirmLabel = "Remove",
                cancelLabel = "Cancel",
                onConfirm = {},
                onCancel = {},
            )
        }
    }

    @Composable
    private fun GalleryContent(content: @Composable () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TuneScoutSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        ) {
            content()
        }
    }
}
