package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import sh.calvin.reorderable.ReorderableCollectionItemScope

private val handleSize = 48.dp
private val handleIconSize = 20.dp

/**
 * The handle is for a finger and says nothing: a drag is not something a screen reader can do, and
 * the row offers "Move up" and "Move down" as actions instead — see [songMoveActions]. Named, it was
 * read at the end of every row as a control that then did nothing.
 *
 * @param dragsOnPress whether the drag starts as soon as the handle is touched. A list that shows
 * the handles only while it is being reordered wants that; one that always shows them waits for a
 * long press, so a scroll that starts on a handle still scrolls.
 */
@Composable
fun ReorderableCollectionItemScope.SongDragHandle(dragsOnPress: Boolean = false) {
    val drag = if (dragsOnPress) Modifier.draggableHandle() else Modifier.longPressDraggableHandle()
    Box(
        modifier = Modifier
            .size(handleSize)
            .then(drag),
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

/**
 * What a row of a list the user can reorder offers accessibility services in place of the drag.
 *
 * @param onMoveUp swaps the row with the one above; null for the first row.
 * @param onMoveDown swaps the row with the one below; null for the last row.
 * @return the moves the row has a neighbour for, labelled for a screen reader.
 */
@Composable
fun songMoveActions(
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
): List<CustomAccessibilityAction> {
    val moveUp = stringResource(R.string.ui_move_up)
    val moveDown = stringResource(R.string.ui_move_down)
    return buildList {
        onMoveUp?.let { move -> add(CustomAccessibilityAction(moveUp) { true.also { move() } }) }
        onMoveDown?.let { move -> add(CustomAccessibilityAction(moveDown) { true.also { move() } }) }
    }
}

/**
 * A song row of a list the user can put in an order of their own — an album, a playlist. A long
 * press anywhere on the row picks it up and drags it, which is also what starts the reordering;
 * while it lasts the sideways swipe is off and the row offers accessibility services the moves in
 * its place. The rest — the handle at its end, a tap that does nothing — is the row's to draw.
 *
 * A row a tap does nothing on no longer merges its title and artist into one item, so while the list
 * is reordered the box does: a screen reader stops on the song once, and finds the moves there.
 *
 * @param isReorderable whether the list can be reordered at all; a long press does nothing when it
 * cannot.
 * @param onReorderStarted a long press picked the row up.
 * @param onMoveUp swaps the row with the one above; null for the first row.
 * @param onMoveDown swaps the row with the one below; null for the last row.
 */
@Composable
fun ReorderableCollectionItemScope.ReorderableSongSwipeBox(
    isReordering: Boolean,
    isFavorite: Boolean,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onReorderStarted: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier,
    isReorderable: Boolean = true,
    content: @Composable () -> Unit,
) {
    val moveActions = songMoveActions(onMoveUp = onMoveUp, onMoveDown = onMoveDown)
    SongSwipeActionsBox(
        isFavorite = isFavorite,
        onAddToQueue = onAddToQueue,
        onToggleFavorite = onToggleFavorite,
        isEnabled = !isReordering,
        modifier = modifier
            .longPressDraggableHandle(enabled = isReorderable, onDragStarted = { onReorderStarted() })
            .semantics(mergeDescendants = isReordering) { if (isReordering) customActions = moveActions },
        content = content,
    )
}
