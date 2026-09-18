package com.quare.tunescout.core.navigation.di

import com.quare.tunescout.core.navigation.ChannelNavigator
import com.quare.tunescout.core.navigation.Navigator
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val navigationModule: Module = module {
    singleOf(::ChannelNavigator) { bind<Navigator>() }
}
