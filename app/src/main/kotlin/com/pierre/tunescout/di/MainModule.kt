package com.pierre.tunescout.di

import com.pierre.tunescout.presentation.viewmodel.MainViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule: Module = module {
    viewModelOf(::MainViewModel)
}
