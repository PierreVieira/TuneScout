package com.pierre.tunescout.feature.widget.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.pierre.tunescout.feature.widget.domain.usecase.ControlWidgetPlayback
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TogglePlayPauseAction :
    ActionCallback,
    KoinComponent {
    private val controlWidgetPlayback: ControlWidgetPlayback by inject()

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        controlWidgetPlayback.togglePlayPause()
    }
}
