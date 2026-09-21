package com.pierre.tunescout.feature.widget.presentation.widget

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Hands the widget picker a rendered preview of each widget.
 *
 * Without one the picker falls back to the launcher icon, which says nothing about what the widget
 * does. `AppWidgetManager` only accepts a rendered preview from Android 15 on, so this publishes
 * nothing below that: the picker shows the `android:previewLayout` of each `appwidget-provider`
 * instead, a static XML copy of the widget (see docs/architecture/theming.md).
 *
 * @property context the application context the AppWidget host is reached through.
 */
internal class WidgetPreviewPublisher(
    private val context: Context,
) {
    /**
     * Publishing once per process is deliberate. The system keeps the last preview it accepted, so
     * there is nothing to refresh while the process lives; a process that starts again is also the
     * only moment the previews can go stale — a new version of the app, or a new device locale.
     */
    fun start(scope: CoroutineScope) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        scope.launch { publishPreviews() }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun publishPreviews() {
        try {
            publishPreview(NowPlayingWidgetReceiver::class)
            publishPreview(NowPlayingShortcutsWidgetReceiver::class)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Could not publish the widget previews: ${exception.message}")
        }
    }

    /**
     * A rate limited call is the expected outcome of a process that restarts often, not a failure:
     * the preview the system already holds is the one this would have drawn again.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun publishPreview(receiver: KClass<out GlanceAppWidgetReceiver>) {
        val result = GlanceAppWidgetManager(context).setWidgetPreviews(receiver)
        if (result == GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED) {
            Log.i(TAG, "Rate limited; ${receiver.simpleName} keeps the preview it has.")
        }
    }

    private companion object {
        const val TAG = "WidgetPreviewPublisher"
    }
}
