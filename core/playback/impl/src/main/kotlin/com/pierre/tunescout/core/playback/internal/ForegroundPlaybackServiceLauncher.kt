package com.pierre.tunescout.core.playback.internal

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

internal class ForegroundPlaybackServiceLauncher(
    private val context: Context,
) : PlaybackServiceLauncher {
    override fun launch() {
        ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java))
    }
}
