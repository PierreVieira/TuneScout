package com.quare.tunescout.di

import com.quare.tunescout.core.database.di.databaseModule
import com.quare.tunescout.core.navigation.di.navigationModule
import com.quare.tunescout.core.network.di.networkModule
import com.quare.tunescout.core.playback.di.playbackModule
import com.quare.tunescout.feature.album.di.albumModule
import com.quare.tunescout.feature.player.di.playerModule
import com.quare.tunescout.feature.songs.di.songsModule
import com.quare.tunescout.feature.splash.di.splashModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    navigationModule,
    networkModule,
    databaseModule,
    playbackModule,
    splashModule,
    songsModule,
    playerModule,
    albumModule,
)
