package com.pierre.tunescout.feature.widget.di

import com.pierre.tunescout.feature.widget.domain.usecase.ControlWidgetPlayback
import com.pierre.tunescout.feature.widget.domain.usecase.ObserveWidgetState
import com.pierre.tunescout.feature.widget.domain.usecase.impl.ControlWidgetPlaybackUseCase
import com.pierre.tunescout.feature.widget.domain.usecase.impl.ObserveWidgetStateUseCase
import com.pierre.tunescout.feature.widget.presentation.widget.NowPlayingWidgetUpdater
import com.pierre.tunescout.feature.widget.presentation.widget.WidgetArtworkLoader
import com.pierre.tunescout.feature.widget.presentation.widget.WidgetPreviewContentFactory
import com.pierre.tunescout.feature.widget.presentation.widget.WidgetPreviewPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

private const val WIDGET_SCOPE = "widgetScope"
private val queueRestoreTimeout = 5.seconds

val widgetModule: Module = module {
    single(named(WIDGET_SCOPE)) { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    factoryOf(::ObserveWidgetStateUseCase).bind<ObserveWidgetState>()
    factory<ControlWidgetPlayback> {
        ControlWidgetPlaybackUseCase(
            observablePlayback = get(),
            transportControls = get(),
            playbackStarter = get(),
            recentlyPlayedLocalDataSource = get(),
            dispatcherProvider = get(),
            restoreTimeout = queueRestoreTimeout,
        )
    }
    single { WidgetArtworkLoader(imageLoader = get(), context = androidContext()) }
    factoryOf(::WidgetPreviewContentFactory)
    single(createdAtStart = true) {
        NowPlayingWidgetUpdater(observeWidgetState = get(), context = androidContext())
            .also { updater -> updater.start(get(named(WIDGET_SCOPE))) }
    }
    single(createdAtStart = true) {
        WidgetPreviewPublisher(context = androidContext())
            .also { publisher -> publisher.start(get(named(WIDGET_SCOPE))) }
    }
}
