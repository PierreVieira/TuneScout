package com.pierre.tunescout.core.navigation.scene

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Lays a detail beside the list it was picked from, when [isTwoPane]. The back stack does not change
 * with the width of the window: `[list, detail]` is the same two entries whether they are drawn side
 * by side or one over the other, so a rotation only ever changes the scene, and Back pops the same
 * entry either way.
 *
 * A detail pushed over another detail replaces it in the right pane rather than covering the list, and
 * Back walks through them in the same pane.
 *
 * The list alone keeps the two panes too, with [emptyDetailPane] beside it. That scene has a key of
 * its own, so opening the first detail and closing the last one still change the scene, as they did
 * when the list alone was a single pane: an entry leaving a scene that stays is only ever a detail
 * swapped for another one.
 *
 * The strategy is recreated when [isTwoPane] changes, which is what makes `NavDisplay` calculate its
 * scenes again: it only does that when the strategies or the entries change.
 *
 * @param T the type of the back stack keys.
 * @property isTwoPane whether the window is wide enough to lay the panes side by side.
 * @property emptyDetailPane what the right pane shows while no detail is open in it.
 */
class ListDetailSceneStrategy<T : Any>(
    private val isTwoPane: Boolean,
    private val emptyDetailPane: @Composable () -> Unit,
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (!isTwoPane) return null
        val topEntry = entries.lastOrNull() ?: return null
        if (topEntry.metadata[ListPaneKey] == true) {
            return ListDetailScene(
                key = EmptyDetailSceneKey(listKey = topEntry.contentKey),
                previousEntries = entries.dropLast(1),
                listEntry = topEntry,
                detailEntry = null,
                emptyDetailPane = emptyDetailPane,
            )
        }
        val listIndex = entries.findListPaneIndexOrNull(
            isListPane = { entry -> entry.metadata[ListPaneKey] == true },
            isDetailPane = { entry -> entry.metadata[DetailPaneKey] == true },
        ) ?: return null
        val listEntry = entries[listIndex]
        return ListDetailScene(
            key = listEntry.contentKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = topEntry,
            emptyDetailPane = emptyDetailPane,
        )
    }

    companion object {
        /** @return the metadata of an entry that detail panes open beside. */
        fun listPane() = metadata {
            put(ListPaneKey, true)
        }

        /** @return the metadata of an entry that opens beside the list under it. */
        fun detailPane() = metadata {
            put(DetailPaneKey, true)
        }

        private object ListPaneKey : NavMetadataKey<Boolean>

        private object DetailPaneKey : NavMetadataKey<Boolean>
    }
}

/**
 * The key of the list alone beside the empty detail pane, told apart from the list with a detail.
 *
 * @property listKey the list's content key.
 */
private data class EmptyDetailSceneKey(
    val listKey: Any,
)

/**
 * The rule of the two panes, shared by the scene strategy, which reads it off the entries' metadata,
 * and by whatever reads the back stack's keys: the top of the stack is a detail, and under the details
 * piled on top of it lies a list.
 *
 * @param isListPane whether an element is a list detail panes open beside.
 * @param isDetailPane whether an element opens beside a list.
 * @return the index of the list the top detail opens beside, or null when the top is not a detail
 * resting on a list.
 */
fun <T> List<T>.findListPaneIndexOrNull(
    isListPane: (T) -> Boolean,
    isDetailPane: (T) -> Boolean,
): Int? {
    if (lastOrNull()?.let(isDetailPane) != true) return null
    val listIndex = indexOfLast { element -> !isDetailPane(element) }
    return listIndex.takeIf { index -> index >= 0 && isListPane(this[index]) }
}
