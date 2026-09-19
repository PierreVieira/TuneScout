package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.miniplayer.presentation.viewmodel.MiniPlayerViewModel
import com.pierre.tunescout.ui.component.getPlayButtonState
import com.pierre.tunescout.ui.component.readableWidth
import com.pierre.tunescout.ui.utils.animation.LocalSharedElementScopes
import com.pierre.tunescout.ui.utils.animation.rememberSharedElementScopes
import org.koin.compose.viewmodel.koinViewModel

/**
 * The bar sits below the content, so it covers the bottom navigation bar and nothing else: a side
 * navigation bar, which landscape puts next to the content, still has to be padded for above.
 */
private val consumedInsets: WindowInsets
    @Composable get() = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)

/**
 * The bar gives its room back to whatever is being typed into: it sits where the keyboard opens, and
 * a landscape window has little enough height without it. This reads the visibility flag rather than
 * the inset, which still measures the navigation bar while the keyboard is closed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun isKeyboardOpen(): Boolean = WindowInsets.isImeVisible

/**
 * The song the bar draws, held at its last value once the bar starts leaving. Opening the player
 * changes what is playing a frame or two later, and a bar that followed that change mid-exit would
 * claim the shared artwork key of the song the list row is already flying.
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

@Composable
fun MiniPlayerScaffold(
    isAllowed: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isVisible = isAllowed && !isKeyboardOpen() && uiState.song != null
    val song = rememberBarSong(song = uiState.song, isVisible = isVisible)
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .then(if (isVisible) Modifier.consumeWindowInsets(consumedInsets) else Modifier),
        ) {
            content()
        }
        // Fade alone, with no expand or shrink: the bar keeps its bounds while it leaves, which is
        // what the artwork flying out of it animates from.
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (song != null) {
                CompositionLocalProvider(
                    LocalSharedElementScopes provides rememberSharedElementScopes(this@AnimatedVisibility),
                ) {
                    // The bar is capped inside the navigation bar padding, not around it, so it
                    // centres on the same axis as the content above rather than on the whole window.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MiniPlayerContent(
                            song = song,
                            playButtonState = getPlayButtonState(
                                isPlaying = uiState.isPlaying,
                                hasEnded = uiState.hasEnded,
                            ),
                            progress = uiState.progress,
                            onEvent = viewModel::onEvent,
                            modifier = Modifier.readableWidth(),
                        )
                    }
                }
            }
        }
    }
}
