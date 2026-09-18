package com.pierre.tunescout.core.playback.di

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.core.playback.internal.ExoPlayerPlaybackController
import com.pierre.tunescout.core.playback.internal.ForegroundPlaybackServiceLauncher
import com.pierre.tunescout.core.playback.internal.PlaybackServiceLauncher
import com.pierre.tunescout.core.playback.internal.RecentlyPlayedRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val PLAYBACK_SCOPE = "playbackScope"

val playbackModule: Module = module {
    single(named(PLAYBACK_SCOPE)) { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single<ExoPlayer> {
        ExoPlayer
            .Builder(androidContext())
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true,
            ).setHandleAudioBecomingNoisy(true)
            .build()
    }
    single<PlaybackServiceLauncher> { ForegroundPlaybackServiceLauncher(context = androidContext()) }
    single<PlaybackController> {
        ExoPlayerPlaybackController(
            player = get(),
            serviceLauncher = get(),
            scope = get(named(PLAYBACK_SCOPE)),
        )
    }
    single(createdAtStart = true) {
        RecentlyPlayedRecorder(
            playbackState = get<PlaybackController>().state,
            recentlyPlayedLocalDataSource = get(),
        ).also { recorder -> recorder.start(get(named(PLAYBACK_SCOPE))) }
    }
}
