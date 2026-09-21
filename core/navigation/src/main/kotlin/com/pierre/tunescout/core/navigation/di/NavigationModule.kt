package com.pierre.tunescout.core.navigation.di

import com.pierre.tunescout.core.navigation.ChannelNavigator
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.deeplink.DeepLinkMatcher
import com.pierre.tunescout.core.navigation.deeplink.SyntheticBackStackFactory
import com.pierre.tunescout.core.navigation.deeplink.TuneScoutDeepLinkMatcher
import com.pierre.tunescout.core.navigation.reorder.ReorderRequests
import com.pierre.tunescout.core.navigation.reorder.SharedFlowReorderRequests
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val navigationModule: Module = module {
    singleOf(::ChannelNavigator).bind<Navigator>()
    singleOf(::TuneScoutDeepLinkMatcher).bind<DeepLinkMatcher>()
    singleOf(::SyntheticBackStackFactory)
    singleOf(::SharedFlowReorderRequests).bind<ReorderRequests>()
}
