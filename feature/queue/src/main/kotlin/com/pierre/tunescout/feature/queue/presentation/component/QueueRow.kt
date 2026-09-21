package com.pierre.tunescout.feature.queue.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.feature.queue.R
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import com.pierre.tunescout.ui.component.SongRow
import com.pierre.tunescout.ui.component.SongRowAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import sh.calvin.reorderable.ReorderableCollectionItemScope

private val handleSize = 48.dp
private val handleIconSize = 20.dp
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
    val moveUp = stringResource(R.string.queue_move_up)
    val moveDown = stringResource(R.string.queue_move_down)
    SongRow(
        title = entry.song.title,
        subtitle = entry.song.artistName,
        artworkUrl = entry.song.artwork.thumbnailUrl,
        onClick = { onEvent(QueueUiEvent.OnEntryClicked(entry.id)) },
        isUnavailable = isUnavailable,
        modifier = modifier.testTag(QUEUE_ENTRY_TAG).semantics {
            customActions = buildList {
                previousEntryId?.let { target -> add(createMoveAction(moveUp, entry.id, target, onEvent)) }
                nextEntryId?.let { target -> add(createMoveAction(moveDown, entry.id, target, onEvent)) }
            }
        },
        trailing = {
            SongRowAction(
                icon = TuneScoutIcons.removeFromQueue,
                contentDescription = stringResource(R.string.queue_remove),
                onClick = { onEvent(QueueUiEvent.OnRemoveClicked(entry.id)) },
            )
            DragHandle()
        },
    )
}

/**
 * The handle is for a finger and says nothing: a drag is not something a screen reader can do, and
 * the row already offers "Move up" and "Move down" as actions. Named, it was read at the end of
 * every row as a control that then did nothing.
 */
@Composable
private fun ReorderableCollectionItemScope.DragHandle() {
    Box(
        modifier = Modifier
            .size(handleSize)
            .longPressDraggableHandle(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = TuneScoutIcons.dragHandle,
            contentDescription = null,
            tint = TuneScoutColors.textTertiary,
            modifier = Modifier.size(handleIconSize),
        )
    }
}

private fun createMoveAction(
    label: String,
    entryId: String,
    targetEntryId: String,
    onEvent: (QueueUiEvent) -> Unit,
): CustomAccessibilityAction = CustomAccessibilityAction(label) {
    onEvent(QueueUiEvent.OnEntryMoved(fromEntryId = entryId, toEntryId = targetEntryId))
    true
}
