package com.pierre.tunescout.di

import com.pierre.tunescout.core.database.di.databaseModule
import com.pierre.tunescout.core.datastore.di.dataStoreModule
import com.pierre.tunescout.core.navigation.di.navigationModule
import com.pierre.tunescout.core.network.di.networkModule
import com.pierre.tunescout.core.playback.di.playbackModule
import com.pierre.tunescout.core.utils.di.utilsModule
import com.pierre.tunescout.feature.addtoplaylist.di.addToPlaylistModule
import com.pierre.tunescout.feature.album.di.albumModule
import com.pierre.tunescout.feature.library.di.libraryModule
import com.pierre.tunescout.feature.miniplayer.di.miniPlayerModule
import com.pierre.tunescout.feature.player.di.playerModule
import com.pierre.tunescout.feature.queue.di.queueModule
import com.pierre.tunescout.feature.songoptions.di.songOptionsModule
import com.pierre.tunescout.feature.songs.di.songsModule
import com.pierre.tunescout.feature.splash.di.splashModule
import com.pierre.tunescout.feature.themeselection.di.themeSelectionModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    mainModule,
    utilsModule,
    navigationModule,
    networkModule,
    databaseModule,
    dataStoreModule,
    playbackModule,
    splashModule,
    songsModule,
    libraryModule,
    songOptionsModule,
    addToPlaylistModule,
    miniPlayerModule,
    playerModule,
    queueModule,
    albumModule,
    themeSelectionModule,
)
