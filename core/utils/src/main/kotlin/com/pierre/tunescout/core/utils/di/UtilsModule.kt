package com.pierre.tunescout.core.utils.di

import com.pierre.tunescout.core.utils.DefaultDispatcherProvider
import com.pierre.tunescout.core.utils.DispatcherProvider
import com.pierre.tunescout.core.utils.IdGenerator
import com.pierre.tunescout.core.utils.UuidIdGenerator
import org.koin.core.module.Module
import org.koin.dsl.module

val utilsModule: Module = module {
    single<IdGenerator> { UuidIdGenerator() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
