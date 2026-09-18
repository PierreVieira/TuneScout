package com.quare.tunescout.di

import com.quare.tunescout.core.navigation.di.navigationModule
import com.quare.tunescout.core.network.di.networkModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    navigationModule,
    networkModule,
)
