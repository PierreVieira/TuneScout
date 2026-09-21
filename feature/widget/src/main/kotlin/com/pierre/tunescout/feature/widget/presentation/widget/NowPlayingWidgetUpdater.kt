package com.pierre.tunescout.feature.widget.presentation.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.pierre.tunescout.feature.widget.domain.usecase.ObserveWidgetState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Pushes the playback state onto the widgets that are already on a home screen.
 *
 * Glance redraws on its own while a widget's session is running, but the launcher ends that
 * session whenever it likes — and the app's process outlives it, because playback keeps a
 * foreground service up. Without this, a song skipped from the notification would leave the widget
 * showing the previous one until something else woke it.
 *
 * @property observeWidgetState the state the widgets draw.
 * @property context the application context the AppWidget host is reached through.
 */
internal class NowPlayingWidgetUpdater(
    private val observeWidgetState: ObserveWidgetState,
    private val context: Context,
) {
    fun start(scope: CoroutineScope) {
        observeWidgetState()
            .onEach { updateWidgets() }
            .launchIn(scope)
    }

    private suspend fun updateWidgets() {
        try {
            NowPlayingWidget().updateAll(context)
            NowPlayingShortcutsWidget().updateAll(context)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Could not update the widgets: ${exception.message}")
        }
    }

    private companion object {
        const val TAG = "NowPlayingWidgetUpdater"
    }
}
