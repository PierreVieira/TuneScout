package com.pierre.tunescout.di

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.pierre.tunescout.presentation.viewmodel.MainViewModel
import okio.FileSystem
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private const val IMAGE_CACHE_DIR = "image_cache"
private const val IMAGE_CACHE_FREE_SPACE_PERCENT = 0.02
private const val IMAGE_CACHE_MIN_BYTES = 64L * 1024 * 1024

/**
 * Coil would configure a disk cache of its own, but where the artwork lands and how little of it
 * can be kept is part of what the app promises offline — so the loader is declared here rather
 * than inherited. The fetcher is named explicitly for the same reason: it would otherwise be
 * picked up from the classpath by a service loader.
 *
 * The cache still grows with the device, as Coil's own default does. What changes is its floor:
 * Coil clamps 2% of the free space to a 10 MB minimum, which on a full device is a few dozen of
 * the 1000×1000 covers the player draws, and artwork is the first thing a list loses offline.
 */
val mainModule: Module = module {
    viewModelOf(::MainViewModel)
    single<ImageLoader> {
        ImageLoader
            .Builder(androidContext())
            .components { add(KtorNetworkFetcherFactory()) }
            .diskCache {
                DiskCache
                    .Builder()
                    .fileSystem(FileSystem.SYSTEM)
                    .directory(androidContext().cacheDir.resolve(IMAGE_CACHE_DIR))
                    .maxSizePercent(IMAGE_CACHE_FREE_SPACE_PERCENT)
                    .minimumMaxSizeBytes(IMAGE_CACHE_MIN_BYTES)
                    .build()
            }.build()
    }
}
