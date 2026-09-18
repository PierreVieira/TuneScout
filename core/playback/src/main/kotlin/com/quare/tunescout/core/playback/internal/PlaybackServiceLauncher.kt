package com.quare.tunescout.core.playback.internal

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

internal fun interface PlaybackServiceLauncher {
    fun launch()
}

internal class ForegroundPlaybackServiceLauncher(
    private val context: Context,
) : PlaybackServiceLauncher {
    override fun launch() {
        ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java))
    }
}
