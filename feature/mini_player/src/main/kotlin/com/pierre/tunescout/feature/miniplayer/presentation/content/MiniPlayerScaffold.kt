package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiAction
import com.pierre.tunescout.feature.miniplayer.presentation.model.MiniPlayerUiState
import com.pierre.tunescout.feature.miniplayer.presentation.viewmodel.MiniPlayerViewModel
import com.pierre.tunescout.ui.component.PlayButtonState
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import com.pierre.tunescout.ui.utils.animation.LocalSharedArtworkDestination
import com.pierre.tunescout.ui.utils.animation.LocalSharedElementScopes
import com.pierre.tunescout.ui.utils.animation.SharedArtworkDestination
import com.pierre.tunescout.ui.utils.animation.rememberSharedElementScopes
import org.koin.compose.viewmodel.koinViewModel

/**
 * The bar sits below the content, so it covers the bottom navigation bar and nothing else: a side
 * navigation bar, which landscape puts next to the content, still has to be padded for above.
 */
private val consumedInsets: WindowInsets
    @Composable get() = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)

/**
 * The song the bar draws, held at its last value once the bar starts leaving. Opening the player
 * changes what is playing a frame or two later, and a bar that followed that change mid-exit would
 * claim the shared artwork key of the song the list row is already flying.
 *
 * @return [song] while [isVisible], and the last song seen once it is not.
 */
@Composable
internal fun rememberBarSong(
    song: Song?,
    isVisible: Boolean,
): Song? {
    var barSong by remember { mutableStateOf(song) }
    if (isVisible) {
        barSong = song
    }
    return barSong
}

/**
 * The bar fades alone, with no expand or shrink: it keeps its bounds while it leaves, which is what
 * the artwork flying out of it animates from.
 *
 * On the way back from the player it waits for the `NavDisplay` to start taking the player away, not
 * only for the back stack to allow it: see [SharedArtworkDestination].
 *
 * It is capped inside the navigation bar padding, not around it, so it centres on the same axis as
 * the content above rather than on the whole window.
 */
@Composable
fun MiniPlayerScaffold(
    isAllowed: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loaded = uiState as? MiniPlayerUiState.Loaded
    val isVisible = isAllowed && loaded != null && !LocalSharedArtworkDestination.current.isStaying
    val song = rememberBarSong(song = loaded?.song, isVisible = isVisible)
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is MiniPlayerUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .consumeWindowInsets(WindowInsets.ime)
                .then(if (isVisible) Modifier.consumeWindowInsets(consumedInsets) else Modifier),
        ) {
            SnackbarBox(hostState = snackbarHostState, modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (song != null) {
                CompositionLocalProvider(
                    LocalSharedElementScopes provides rememberSharedElementScopes(this@AnimatedVisibility),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MiniPlayerContent(
                            song = song,
                            playButtonState = PlayButtonState.of(
                                isPlaying = loaded?.isPlaying == true,
                                hasEnded = loaded?.hasEnded == true,
                            ),
                            progress = loaded?.progress ?: 0f,
                            onEvent = viewModel::onEvent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
