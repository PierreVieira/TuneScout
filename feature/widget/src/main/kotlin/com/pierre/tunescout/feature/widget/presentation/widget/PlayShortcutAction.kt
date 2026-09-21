package com.pierre.tunescout.feature.widget.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.pierre.tunescout.feature.widget.domain.usecase.ControlWidgetPlayback
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlayShortcutAction :
    ActionCallback,
    KoinComponent {
    private val controlWidgetPlayback: ControlWidgetPlayback by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val songId = parameters[SONG_ID] ?: return
        controlWidgetPlayback.playSong(songId)
    }

    companion object {
        /**
         * The song a shortcut stands for. Glance hands the parameters back to the callback when
         * the tap arrives, which is the only way a tile can say which one it was.
         */
        val SONG_ID: ActionParameters.Key<Long> = ActionParameters.Key("songId")
    }
}
