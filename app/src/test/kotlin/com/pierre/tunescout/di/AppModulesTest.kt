package com.pierre.tunescout.di

import android.content.Context
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloaderFactory
import androidx.media3.exoplayer.offline.WritableDownloadIndex
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.feature.addtoplaylist.presentation.viewmodel.AddToPlaylistViewModel
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumOptionsViewModel
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumViewModel
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.presentation.viewmodel.CollectionOptionsViewModel
import com.pierre.tunescout.feature.library.presentation.viewmodel.CollectionViewModel
import com.pierre.tunescout.feature.player.presentation.viewmodel.PlayerViewModel
import com.pierre.tunescout.feature.songoptions.presentation.viewmodel.SongOptionsViewModel
import kotlinx.coroutines.flow.Flow
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import kotlin.random.Random
import kotlin.time.Duration

class AppModulesTest {
    /**
     * Not resolved by type: [Flow], [Duration] and [Random] are passed as literals inside their
     * definitions, and the [Context] is the one `androidContext()` hands over when the app starts.
     * The [DownloadManager] is built from the caches inside its definition, but the verification
     * reads its other constructor, the one taking a [WritableDownloadIndex] and a
     * [DownloaderFactory].
     */
    private val literalParameterTypes = listOf(
        Flow::class,
        Duration::class,
        Random::class,
        Context::class,
        WritableDownloadIndex::class,
        DownloaderFactory::class,
    )

    /**
     * Walks every constructor reachable from [appModules] and fails when a parameter has no
     * matching definition. The modules are merged into one, because verifying them one by one
     * would hide the cross-module edges (a feature reaching into `core:network:impl`, for instance).
     */
    @Test
    fun `WHEN verifying the graph THEN every constructor dependency has a definition`() {
        module { includes(appModules) }.verify(
            extraTypes = literalParameterTypes,
            injections = injectedParameters(
                definition<AlbumViewModel>(AlbumRoute::class),
                definition<AlbumOptionsViewModel>(AlbumOptionsRoute::class),
                definition<PlayerViewModel>(PlayerRoute::class),
                definition<SongOptionsViewModel>(SongOptionsRoute::class),
                definition<AddToPlaylistViewModel>(AddToPlaylistRoute::class),
                definition<CollectionViewModel>(CollectionKey::class),
                definition<CollectionOptionsViewModel>(CollectionKey::class),
            ),
        )
    }
}
