package com.pierre.tunescout.feature.queue.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.feature.queue.R
import com.pierre.tunescout.feature.queue.presentation.component.QueueRow
import com.pierre.tunescout.feature.queue.presentation.model.QueueContextTitle
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiState
import com.pierre.tunescout.ui.component.NowPlayingState
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun QueueContent(
    uiState: QueueUiState,
    onEvent: (QueueUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        SheetHeading(contextTitle = uiState.contextTitle?.resolve())
        if (uiState.isEmpty) {
            StateMessage(
                title = stringResource(R.string.queue_empty_title),
                description = stringResource(R.string.queue_empty_description),
            )
        } else {
            QueueList(uiState = uiState, onEvent = onEvent)
        }
    }
}

@Composable
private fun QueueContextTitle.resolve(): String = when (this) {
    is QueueContextTitle.Custom -> name
    QueueContextTitle.LikedSongs -> stringResource(R.string.queue_context_liked_songs)
    QueueContextTitle.DownloadedSongs -> stringResource(R.string.queue_context_downloaded_songs)
    QueueContextTitle.RecentlyPlayed -> stringResource(R.string.queue_context_recently_played)
}

@Composable
private fun SheetHeading(contextTitle: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.screen, vertical = TuneScoutSpacing.small),
    ) {
        Text(
            text = stringResource(R.string.queue_title),
            style = MaterialTheme.typography.titleLarge,
            color = TuneScoutColors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        if (contextTitle != null) {
            Text(
                text = stringResource(R.string.queue_playing_from, contextTitle),
                style = MaterialTheme.typography.bodySmall,
                color = TuneScoutColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QueueList(
    uiState: QueueUiState,
    onEvent: (QueueUiEvent) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromId = from.key as? String
        val toId = to.key as? String
        if (fromId != null && toId != null) {
            onEvent(QueueUiEvent.OnEntryMoved(fromEntryId = fromId, toEntryId = toId))
        }
    }
    val reorderable = uiState.queuedByUser + uiState.upNext
    val upNextLabel = uiState.contextTitle
        ?.resolve()
        ?.let { title -> stringResource(R.string.queue_next_from, title) }
        ?: stringResource(R.string.queue_next_up)
    val queuedLabel = stringResource(R.string.queue_next_in_queue)
    val nowPlayingLabel = stringResource(R.string.queue_now_playing)
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = TuneScoutSpacing.screen,
            end = TuneScoutSpacing.medium,
            bottom = TuneScoutSpacing.large,
        ),
    ) {
        uiState.nowPlaying?.let { entry ->
            sectionLabel(key = "now-playing-label", text = nowPlayingLabel)
            item(key = "now-playing") {
                SongRow(
                    title = entry.song.title,
                    subtitle = entry.song.artistName,
                    artworkUrl = entry.song.artwork.thumbnailUrl,
                    onClick = { onEvent(QueueUiEvent.OnNowPlayingClicked) },
                    onClickLabel = null,
                    nowPlaying = NowPlayingState.of(
                        isCurrentSong = !uiState.hasEnded,
                        isPlaying = uiState.isPlaying,
                    ),
                    isUnavailable = entry.song.id in uiState.unplayableSongIds,
                )
            }
        }
        if (uiState.queuedByUser.isNotEmpty()) {
            sectionLabel(key = "queued-label", text = queuedLabel)
            reorderableEntries(uiState.queuedByUser, uiState.unplayableSongIds, reorderable, reorderableState, onEvent)
        }
        if (uiState.upNext.isNotEmpty()) {
            sectionLabel(key = "up-next-label", text = upNextLabel)
            reorderableEntries(uiState.upNext, uiState.unplayableSongIds, reorderable, reorderableState, onEvent)
        }
    }
}

private fun LazyListScope.sectionLabel(
    key: String,
    text: String,
) {
    item(key = key) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TuneScoutColors.textSecondary,
            modifier = Modifier
                .padding(top = TuneScoutSpacing.medium, bottom = TuneScoutSpacing.extraSmall)
                .semantics { heading() },
        )
    }
}

private fun LazyListScope.reorderableEntries(
    entries: List<QueueEntry>,
    unplayableSongIds: Set<Long>,
    reorderable: List<QueueEntry>,
    reorderableState: ReorderableLazyListState,
    onEvent: (QueueUiEvent) -> Unit,
) {
    items(items = entries, key = { entry -> entry.id }) { entry ->
        val position = reorderable.indexOfFirst { candidate -> candidate.id == entry.id }
        ReorderableItem(reorderableState, key = entry.id) {
            QueueRow(
                entry = entry,
                previousEntryId = reorderable.getOrNull(position - 1)?.id,
                nextEntryId = reorderable.getOrNull(position + 1)?.id,
                isUnavailable = entry.song.id in unplayableSongIds,
                onEvent = onEvent,
            )
        }
    }
}
