package com.pierre.tunescout.core.network.di

import android.net.ConnectivityManager
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.CountryProvider
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.network.internal.ConnectivityNetworkMonitor
import com.pierre.tunescout.core.network.internal.HttpClientFactory
import com.pierre.tunescout.core.network.internal.KtorAlbumRemoteDataSource
import com.pierre.tunescout.core.network.internal.KtorSongSearchRemoteDataSource
import com.pierre.tunescout.core.network.internal.LocaleCountryProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

private const val HTTP_CACHE_DIR = "http_cache"

val networkModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    singleOf(::HttpClientFactory)
    single {
        get<HttpClientFactory>().create(
            engine = get(),
            cacheDir = File(androidContext().cacheDir, HTTP_CACHE_DIR),
        )
    }
    single<ConnectivityManager> {
        checkNotNull(androidContext().getSystemService(ConnectivityManager::class.java)) {
            "The device has no ConnectivityManager"
        }
    }
    singleOf(::ConnectivityNetworkMonitor).bind<NetworkMonitor>()
    singleOf(::LocaleCountryProvider).bind<CountryProvider>()
    singleOf(::KtorSongSearchRemoteDataSource).bind<SongSearchRemoteDataSource>()
    singleOf(::KtorAlbumRemoteDataSource).bind<AlbumRemoteDataSource>()
}
