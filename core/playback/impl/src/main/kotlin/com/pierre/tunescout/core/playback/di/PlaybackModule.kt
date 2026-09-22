@file:OptIn(UnstableApi::class)

package com.pierre.tunescout.core.playback.di

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ShuffleOrder
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.playback.ContextStarter
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservableDownloads
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.QueueControls
import com.pierre.tunescout.core.playback.SongPlayback
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.playback.internal.AndroidMediaItemFactory
import com.pierre.tunescout.core.playback.internal.ArtworkCachingDownloadCommands
import com.pierre.tunescout.core.playback.internal.ConnectivityPlayableSongs
import com.pierre.tunescout.core.playback.internal.DownloadCommands
import com.pierre.tunescout.core.playback.internal.DownloadReconciler
import com.pierre.tunescout.core.playback.internal.ExoPlayerPlaybackController
import com.pierre.tunescout.core.playback.internal.ForegroundPlaybackServiceLauncher
import com.pierre.tunescout.core.playback.internal.MediaButtonSpecFactory
import com.pierre.tunescout.core.playback.internal.MediaCacheDataSourceFactory
import com.pierre.tunescout.core.playback.internal.MediaCachePreviewCache
import com.pierre.tunescout.core.playback.internal.MediaItemFactory
import com.pierre.tunescout.core.playback.internal.PlaybackFavoriteController
import com.pierre.tunescout.core.playback.internal.PlaybackQueue
import com.pierre.tunescout.core.playback.internal.PlaybackServiceLauncher
import com.pierre.tunescout.core.playback.internal.PlaybackSessionKeeper
import com.pierre.tunescout.core.playback.internal.PreviewCache
import com.pierre.tunescout.core.playback.internal.QueueTimelineFactory
import com.pierre.tunescout.core.playback.internal.ReachableSongPlayback
import com.pierre.tunescout.core.playback.internal.RecentlyPlayedRecorder
import com.pierre.tunescout.core.playback.internal.RestorablePlayback
import com.pierre.tunescout.core.playback.internal.ServiceDownloadCommands
import com.pierre.tunescout.core.playback.internal.SongDownloadTracker
import com.pierre.tunescout.core.playback.internal.TrackedDownloadStates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

internal const val PLAYBACK_SCOPE = "playbackScope"
private const val MEDIA_CACHE_DIR = "media_cache"
private const val MEDIA_CACHE_MAX_BYTES = 128L * 1024 * 1024
private const val DOWNLOAD_CACHE = "downloadCache"
private const val DOWNLOAD_CACHE_DIR = "downloads"
private const val MAX_PARALLEL_DOWNLOADS = 3
private val sessionSaveInterval = 5.seconds

val playbackModule: Module = module {
    single(named(PLAYBACK_SCOPE)) { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single<DatabaseProvider> { StandaloneDatabaseProvider(androidContext()) }
    single<Cache> {
        SimpleCache(
            File(androidContext().cacheDir, MEDIA_CACHE_DIR),
            LeastRecentlyUsedCacheEvictor(MEDIA_CACHE_MAX_BYTES),
            get<DatabaseProvider>(),
        )
    }
    single<Cache>(named(DOWNLOAD_CACHE)) {
        SimpleCache(
            File(androidContext().filesDir, DOWNLOAD_CACHE_DIR),
            NoOpCacheEvictor(),
            get<DatabaseProvider>(),
        )
    }
    single<DataSource.Factory> { DefaultDataSource.Factory(androidContext()) }
    single {
        MediaCacheDataSourceFactory(
            cache = get(),
            downloadCache = get(named(DOWNLOAD_CACHE)),
            upstreamFactory = get(),
        )
    }
    single<Executor> { Executors.newFixedThreadPool(MAX_PARALLEL_DOWNLOADS) }
    single<DownloadManager> {
        DownloadManager(
            androidContext(),
            get<DatabaseProvider>(),
            get<Cache>(named(DOWNLOAD_CACHE)),
            get<MediaCacheDataSourceFactory>().createDownloadUpstreamFactory(),
            get<Executor>(),
        ).apply {
            maxParallelDownloads = MAX_PARALLEL_DOWNLOADS
            resumeDownloads()
        }
    }
    single<DownloadCommands> {
        ArtworkCachingDownloadCommands(
            delegate = ServiceDownloadCommands(context = androidContext(), downloadManager = get()),
            imageLoader = get(),
            context = androidContext(),
            scope = get(named(PLAYBACK_SCOPE)),
        )
    }
    single(createdAtStart = true) {
        SongDownloadTracker(downloadManager = get(), dispatcherProvider = get())
            .also { tracker -> tracker.start(get(named(PLAYBACK_SCOPE))) }
    }
    single<ObservableDownloads> { get<SongDownloadTracker>() }
    single<TrackedDownloadStates> { get<SongDownloadTracker>() }
    single(createdAtStart = true) {
        DownloadReconciler(
            downloadLocalDataSource = get(),
            tracker = get(),
            downloadCommands = get(),
        ).also { reconciler -> reconciler.start(get(named(PLAYBACK_SCOPE))) }
    }
    single<PreviewCache> { MediaCachePreviewCache(cache = get()) }
    single {
        ConnectivityPlayableSongs(
            previewCache = get(),
            networkMonitor = get(),
            observableDownloads = get(),
            scope = get(named(PLAYBACK_SCOPE)),
        )
    }
    single<PlayableSongs> { get<ConnectivityPlayableSongs>() }
    single<ObservablePlayableSongs> { get<ConnectivityPlayableSongs>() }
    single<ExoPlayer> {
        ExoPlayer
            .Builder(androidContext())
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(get<MediaCacheDataSourceFactory>().createDataSourceFactory()),
            ).setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true,
            ).setHandleAudioBecomingNoisy(true)
            .setMaxSeekToPreviousPositionMs(PlaybackState.previousSongWindow.inWholeMilliseconds)
            .build()
            .apply { keepQueueOrderWhenShuffled() }
    }
    single<PlaybackServiceLauncher> { ForegroundPlaybackServiceLauncher(context = androidContext()) }
    single<MediaItemFactory> { AndroidMediaItemFactory() }
    single { QueueTimelineFactory(idGenerator = get(), random = Random.Default) }
    single { PlaybackQueue(player = get(), mediaItemFactory = get(), timelineFactory = get()) }
    single {
        ExoPlayerPlaybackController(
            player = get(),
            serviceLauncher = get(),
            queue = get(),
            scope = get(named(PLAYBACK_SCOPE)),
        )
    }
    single<ObservablePlayback> { get<ExoPlayerPlaybackController>() }
    single<PlaybackStarter> { get<ExoPlayerPlaybackController>() }
    single<ContextStarter> { get<ExoPlayerPlaybackController>() }
    single<Enqueuer> { get<ExoPlayerPlaybackController>() }
    single<QueueControls> { get<ExoPlayerPlaybackController>() }
    single<TransportControls> { get<ExoPlayerPlaybackController>() }
    single<RestorablePlayback> { get<ExoPlayerPlaybackController>() }
    single<SongPlayback> { ReachableSongPlayback(playbackStarter = get(), playableSongs = get()) }
    single(createdAtStart = true) {
        PlaybackSessionKeeper(
            observablePlayback = get(),
            restorablePlayback = get(),
            playbackSessionLocalDataSource = get(),
            saveInterval = sessionSaveInterval,
        ).also { keeper -> keeper.start(get(named(PLAYBACK_SCOPE))) }
    }
    single(createdAtStart = true) {
        RecentlyPlayedRecorder(
            playbackState = get<ObservablePlayback>().observePlaybackState(),
            recentlyPlayedLocalDataSource = get(),
        ).also { recorder -> recorder.start(get(named(PLAYBACK_SCOPE))) }
    }
    single { MediaButtonSpecFactory() }
    single {
        PlaybackFavoriteController(
            playbackState = get<ObservablePlayback>().observePlaybackState(),
            favoriteSongLocalDataSource = get(),
        )
    }
}

/**
 * The queue shuffles its own entries, so the order the queue screen lists is the order that plays.
 * The player's shuffle mode stays on while it does — it is what the media session shows the
 * notification and the lock screen — but with an order that leaves the queue's as it is instead of
 * shuffling it a second time.
 */
private fun ExoPlayer.keepQueueOrderWhenShuffled() {
    setShuffleOrder(ShuffleOrder.UnshuffledShuffleOrder(0))
}
