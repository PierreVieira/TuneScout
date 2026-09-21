package com.pierre.tunescout.core.playback.di

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.QueueControls
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.playback.internal.AndroidMediaItemFactory
import com.pierre.tunescout.core.playback.internal.ConnectivityPlayableSongs
import com.pierre.tunescout.core.playback.internal.ExoPlayerPlaybackController
import com.pierre.tunescout.core.playback.internal.ForegroundPlaybackServiceLauncher
import com.pierre.tunescout.core.playback.internal.MediaCacheDataSourceFactory
import com.pierre.tunescout.core.playback.internal.MediaCachePreviewCache
import com.pierre.tunescout.core.playback.internal.MediaItemFactory
import com.pierre.tunescout.core.playback.internal.PlaybackFavoriteController
import com.pierre.tunescout.core.playback.internal.PlaybackQueue
import com.pierre.tunescout.core.playback.internal.PlaybackServiceLauncher
import com.pierre.tunescout.core.playback.internal.PlaybackSessionKeeper
import com.pierre.tunescout.core.playback.internal.PreviewCache
import com.pierre.tunescout.core.playback.internal.QueueTimelineFactory
import com.pierre.tunescout.core.playback.internal.RecentlyPlayedRecorder
import com.pierre.tunescout.core.playback.internal.RestorablePlayback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import kotlin.time.Duration.Companion.seconds

internal const val PLAYBACK_SCOPE = "playbackScope"
private const val MEDIA_CACHE_DIR = "media_cache"
private const val MEDIA_CACHE_MAX_BYTES = 128L * 1024 * 1024
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
    single<DataSource.Factory> { DefaultDataSource.Factory(androidContext()) }
    singleOf(::MediaCacheDataSourceFactory)
    single<PreviewCache> { MediaCachePreviewCache(cache = get()) }
    single<PlayableSongs> {
        ConnectivityPlayableSongs(
            previewCache = get(),
            networkMonitor = get(),
            scope = get(named(PLAYBACK_SCOPE)),
        )
    }
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
    }
    single<PlaybackServiceLauncher> { ForegroundPlaybackServiceLauncher(context = androidContext()) }
    single<MediaItemFactory> { AndroidMediaItemFactory() }
    singleOf(::QueueTimelineFactory)
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
    single<Enqueuer> { get<ExoPlayerPlaybackController>() }
    single<QueueControls> { get<ExoPlayerPlaybackController>() }
    single<TransportControls> { get<ExoPlayerPlaybackController>() }
    single<RestorablePlayback> { get<ExoPlayerPlaybackController>() }
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
    single {
        PlaybackFavoriteController(
            playbackState = get<ObservablePlayback>().observePlaybackState(),
            favoriteSongLocalDataSource = get(),
        )
    }
}
