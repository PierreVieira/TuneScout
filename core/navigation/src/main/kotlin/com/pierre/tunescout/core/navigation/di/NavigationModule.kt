package com.pierre.tunescout.core.navigation.di

import com.pierre.tunescout.core.navigation.ChannelNavigator
import com.pierre.tunescout.core.navigation.Navigator
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val navigationModule: Module = module {
    singleOf(::ChannelNavigator).bind<Navigator>()
}
