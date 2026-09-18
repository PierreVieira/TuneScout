package com.pierre.tunescout.core.network.di

import com.pierre.tunescout.core.network.CountryProvider
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.network.internal.KtorITunesRemoteDataSource
import com.pierre.tunescout.core.network.internal.LocaleCountryProvider
import com.pierre.tunescout.core.network.internal.createHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { createHttpClient(engine = get()) }
    singleOf(::LocaleCountryProvider).bind<CountryProvider>()
    singleOf(::KtorITunesRemoteDataSource).bind<ITunesRemoteDataSource>()
}
