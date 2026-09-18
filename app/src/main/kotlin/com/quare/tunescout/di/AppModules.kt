package com.quare.tunescout.di

import com.quare.tunescout.core.navigation.di.navigationModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    navigationModule,
)
