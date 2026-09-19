package com.pierre.tunescout.feature.queue.di

import com.pierre.tunescout.feature.queue.presentation.viewmodel.QueueViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val queueModule: Module = module {
    viewModelOf(::QueueViewModel)
}
