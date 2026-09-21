package com.pierre.tunescout.ui.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * A ViewModel that reports one-shot actions to its screen through [uiAction] — the emitting half of
 * the mechanism [ActionCollector] collects.
 *
 * The flow replays nothing: an action reaches whoever is collecting the moment it is emitted, and a
 * screen that subscribes later never sees it. That is what keeps the action one-shot, and it is also
 * why emitting suspends, so [emitAction] carries it on `viewModelScope` and it dies with the
 * ViewModel.
 *
 * Generic in the action type so the type itself stays the feature's own, as every feature's
 * `UiAction` is: the plumbing is shared, the vocabulary is not.
 *
 * @param A the feature's `UiAction` type.
 */
abstract class ActionViewModel<A> : ViewModel() {
    val uiAction: SharedFlow<A>
        field = MutableSharedFlow<A>()

    protected fun emitAction(action: A) {
        viewModelScope.launch { uiAction.emit(action) }
    }
}
