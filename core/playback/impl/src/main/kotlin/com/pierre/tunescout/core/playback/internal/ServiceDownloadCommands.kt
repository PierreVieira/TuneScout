package com.pierre.tunescout.core.playback.internal

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.pierre.tunescout.core.model.Song

/**
 * Sends each download through [SongDownloadService], which keeps the process alive and shows the
 * progress while the files arrive. A download is keyed by the song's id and stored under its
 * preview's url, the same key the player reads the preview by.
 *
 * @property context what starts the service.
 * @property downloadManager what takes a download directly when the service cannot be started.
 */
@OptIn(UnstableApi::class)
internal class ServiceDownloadCommands(
    private val context: Context,
    private val downloadManager: DownloadManager,
) : DownloadCommands {
    /**
     * Android refuses to start a foreground service from the background, and a like sent from the
     * lock screen can change what is wanted while no screen is open. The download is then handed
     * to the manager itself: it still arrives while the process lives, only without the service.
     */
    override fun add(song: Song) {
        val request = DownloadRequest.Builder(song.id.toString(), Uri.parse(song.previewUrl)).build()
        try {
            DownloadService.sendAddDownload(context, SongDownloadService::class.java, request, true)
        } catch (exception: IllegalStateException) {
            downloadManager.addDownload(request)
        }
    }

    override fun remove(songId: Long) {
        downloadManager.removeDownload(songId.toString())
    }
}
