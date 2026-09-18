package com.quare.tunescout.core.network.di

import com.quare.tunescout.core.network.CountryProvider
import com.quare.tunescout.core.network.ITunesRemoteDataSource
import com.quare.tunescout.core.network.internal.KtorITunesRemoteDataSource
import com.quare.tunescout.core.network.internal.LocaleCountryProvider
import com.quare.tunescout.core.network.internal.createHttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule: Module = module {
    single { createHttpClient(engine = OkHttp.create()) }
    singleOf(::LocaleCountryProvider) { bind<CountryProvider>() }
    singleOf(::KtorITunesRemoteDataSource) { bind<ITunesRemoteDataSource>() }
}
