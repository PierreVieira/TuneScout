package com.pierre.tunescout.feature.queue.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.feature.queue.R
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import com.pierre.tunescout.ui.component.SongDragHandle
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.component.songMoveActions
import sh.calvin.reorderable.ReorderableCollectionItemScope

private const val QUEUE_ENTRY_TAG = "queue_entry"

@Composable
internal fun ReorderableCollectionItemScope.QueueRow(
    entry: QueueEntry,
    previousEntryId: String?,
    nextEntryId: String?,
    isUnavailable: Boolean,
    onEvent: (QueueUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val moveTo: (String) -> () -> Unit = { target ->
        { onEvent(QueueUiEvent.OnEntryMoved(fromEntryId = entry.id, toEntryId = target)) }
    }
    val moveActions = songMoveActions(
        onMoveUp = previousEntryId?.let(moveTo),
        onMoveDown = nextEntryId?.let(moveTo),
    )
    SongRow(
        title = entry.song.title,
        subtitle = entry.song.artistName,
        artworkUrl = entry.song.artwork.thumbnailUrl,
        onClick = { onEvent(QueueUiEvent.OnEntryClicked(entry.id)) },
        isUnavailable = isUnavailable,
        modifier = modifier.testTag(QUEUE_ENTRY_TAG).semantics { customActions = moveActions },
        trailing = {
            SongRowAction(
                icon = TuneScoutIcons.removeFromQueue,
                contentDescription = stringResource(R.string.queue_remove),
                onClick = { onEvent(QueueUiEvent.OnRemoveClicked(entry.id)) },
            )
            SongDragHandle()
        },
    )
}
