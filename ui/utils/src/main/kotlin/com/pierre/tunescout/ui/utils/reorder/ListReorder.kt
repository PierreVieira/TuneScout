package com.pierre.tunescout.ui.utils.reorder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * A list the user drags into an order of their own, and whether the screen is in the mode that
 * shows the handles for it.
 *
 * A drag moves a row many times before it is let go, and every move is stored. The rows are drawn
 * from the order kept here rather than from what the store writes back, so a write still on its way
 * never makes a row jump back under the finger. The writes run one after the other, and one that
 * falls behind is skipped for the order that replaced it.
 *
 * @param T the items of the list.
 * @param K what tells one item from another.
 * @property keyOf the key of an item.
 * @param scope where the order is stored; the ViewModel's, so a write outlives the screen but not
 * the ViewModel.
 * @param persist stores the whole order, every key in it.
 */
class ListReorder<T, K>(
    private val keyOf: (T) -> K,
    scope: CoroutineScope,
    persist: suspend (List<K>) -> Unit,
) {
    private val order = MutableStateFlow<List<K>?>(null)

    val isReordering: StateFlow<Boolean>
        field = MutableStateFlow(false)

    init {
        scope.launch { order.filterNotNull().collect(persist) }
    }

    fun start() {
        isReordering.value = true
    }

    fun finish() {
        isReordering.value = false
    }

    /**
     * @param rearrange puts the list [source] carries back into it, once arranged.
     * @return [source], with its list in the order the user dragged it into.
     */
    fun <S> observeArranged(
        source: Flow<S>,
        rearrange: (S, arrange: (List<T>) -> List<T>) -> S,
    ): Flow<S> = combine(source, order) { value, keys -> rearrange(value) { items -> items.sortInto(keys) } }

    fun observeArranged(items: Flow<List<T>>): Flow<List<T>> = observeArranged(items) { list, arrange -> arrange(list) }

    /**
     * Puts the item under [from] where the one under [to] is, the way a dragged row takes the place
     * of the row it is dropped on.
     *
     * @param items the list as it is on screen.
     */
    fun move(
        items: List<T>,
        from: K,
        to: K,
    ) {
        val keys = items.map(keyOf)
        val fromIndex = keys.indexOf(from)
        val toIndex = keys.indexOf(to)
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return
        order.value = keys.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    /**
     * An item the order does not know — one added since — keeps its place after the ones it does.
     *
     * @return the list in the order of [keys], or as it is when there is no order yet.
     */
    private fun List<T>.sortInto(keys: List<K>?): List<T> {
        if (keys == null) return this
        val positions = keys.withIndex().associate { (index, key) -> key to index }
        return sortedBy { item -> positions[keyOf(item)] ?: Int.MAX_VALUE }
    }
}
