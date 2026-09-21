package com.pierre.tunescout.core.navigation.scene

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.pierre.tunescout.ui.component.ListDetailScaffold
import com.pierre.tunescout.ui.component.createCrossFadeTransition
import com.pierre.tunescout.ui.utils.navigation.LocalIsBesideDetailPane

/**
 * [listEntry] and, beside it, [detailEntry].
 *
 * The key is the list's, so a detail swapped for another one keeps the scene: the list stays where it
 * is, not faded out and back in, and only the detail pane crossfades. Opening the first detail or
 * closing the last one does change the scene, from or to the list alone.
 *
 * @param T the type of the back stack keys.
 * @property key the list's content key, which identifies the scene.
 * @property previousEntries the entries to go back to, every one but the detail on top.
 * @property listEntry the entry drawn in the list pane.
 * @property detailEntry the entry drawn in the detail pane.
 */
internal data class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    private val listEntry: NavEntry<T>,
    private val detailEntry: NavEntry<T>,
) : Scene<T> {
    override val entries: List<NavEntry<T>> = listOf(listEntry, detailEntry)

    override val content: @Composable () -> Unit = { ListDetailSceneContent(listEntry, detailEntry) }
}

@Composable
private fun <T : Any> ListDetailSceneContent(
    listEntry: NavEntry<T>,
    detailEntry: NavEntry<T>,
) {
    ListDetailScaffold(
        listPane = {
            CompositionLocalProvider(LocalIsBesideDetailPane provides true) {
                listEntry.Content()
            }
        },
        detailPane = {
            AnimatedContent(
                targetState = detailEntry,
                transitionSpec = { createCrossFadeTransition() },
                contentKey = { entry -> entry.contentKey },
            ) { entry ->
                entry.Content()
            }
        },
    )
}

/**
 * For a list that runs a navigation of its own — the tab host's nested `NavDisplay`: while a detail
 * is open beside it, Back closes the detail, and the list's own back handling stands aside.
 *
 * Back handlers are asked most recent first, and the list was composed before the detail was pushed,
 * so its handler would win over the root `NavDisplay`'s and switch tabs instead. Moving the list into
 * the two panes does not re-register it either: an entry is movable content, and its handler stays
 * bound to the dispatcher it was first registered with. So [content] gets a dispatcher of its own from
 * the start, and it is that dispatcher which is turned off while a detail is open.
 *
 * @param content the list's own navigation.
 */
@Composable
fun DeferBackToDetailPaneScaffold(content: @Composable () -> Unit) {
    val dispatcherOwner = rememberNavigationEventDispatcherOwner(enabled = !LocalIsBesideDetailPane.current)
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides dispatcherOwner) {
        content()
    }
}
