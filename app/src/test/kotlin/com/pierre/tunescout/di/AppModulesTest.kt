package com.pierre.tunescout.di

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.feature.addtoplaylist.presentation.viewmodel.AddToPlaylistViewModel
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumViewModel
import com.pierre.tunescout.feature.library.presentation.viewmodel.CollectionViewModel
import com.pierre.tunescout.feature.player.presentation.viewmodel.PlayerViewModel
import com.pierre.tunescout.feature.songoptions.presentation.viewmodel.SongOptionsViewModel
import kotlinx.coroutines.flow.Flow
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import kotlin.time.Duration

class AppModulesTest {
    /**
     * Walks every constructor reachable from [appModules] and fails when a parameter has no
     * matching definition. The modules are merged into one, because verifying them one by one
     * would hide the cross-module edges (a feature reaching into `core:network`, for instance).
     */
    @Test
    fun `WHEN verifying the graph THEN every constructor dependency has a definition`() {
        module { includes(appModules) }.verify(
            // Passed as literals inside their definitions, not resolved by type.
            extraTypes = listOf(Flow::class, Duration::class),
            injections = injectedParameters(
                definition<AlbumViewModel>(AlbumRoute::class),
                definition<PlayerViewModel>(PlayerRoute::class),
                definition<SongOptionsViewModel>(SongOptionsRoute::class),
                definition<AddToPlaylistViewModel>(AddToPlaylistRoute::class),
                definition<CollectionViewModel>(LibraryItemKey::class),
            ),
        )
    }
}
