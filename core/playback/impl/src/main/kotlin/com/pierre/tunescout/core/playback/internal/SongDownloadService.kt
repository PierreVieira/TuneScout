package com.pierre.tunescout.core.playback.internal

import android.app.Notification
import android.app.PendingIntent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.pierre.tunescout.core.playback.R
import org.koin.android.ext.android.inject

/**
 * Runs while songs are downloading, with a notification of their progress, and stops once they are
 * done. There is no scheduler: downloads left waiting for a connection when the process dies carry
 * on the next time the app starts.
 */
@OptIn(UnstableApi::class)
internal class SongDownloadService :
    DownloadService(
        NOTIFICATION_ID,
        DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        CHANNEL_ID,
        R.string.playback_download_channel_name,
        0,
    ) {
    private val manager: DownloadManager by inject()
    private val notificationHelper: DownloadNotificationHelper by lazy { DownloadNotificationHelper(this, CHANNEL_ID) }

    override fun getDownloadManager(): DownloadManager = manager

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification = notificationHelper.buildProgressNotification(
        this,
        R.drawable.ic_download_notification,
        createLaunchIntent(),
        null,
        downloads,
        notMetRequirements,
    )

    private fun createLaunchIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        const val NOTIFICATION_ID = 2
        const val CHANNEL_ID = "downloads"
    }
}
