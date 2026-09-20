package com.pierre.tunescout

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.pierre.tunescout.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class TuneScoutApplication :
    Application(),
    SingletonImageLoader.Factory,
    KoinComponent {
    private val imageLoader: ImageLoader by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TuneScoutApplication)
            modules(appModules)
        }
    }

    /**
     * Coil asks for its singleton the first time something draws an image, which is after Koin has
     * started.
     *
     * @return the loader the graph configured, so every `AsyncImage` in the app draws through the
     * disk cache declared there.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader = imageLoader
}
