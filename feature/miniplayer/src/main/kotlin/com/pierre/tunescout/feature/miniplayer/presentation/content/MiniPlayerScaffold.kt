package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.miniplayer.presentation.viewmodel.MiniPlayerViewModel
import com.pierre.tunescout.ui.component.getPlayButtonState
import com.pierre.tunescout.ui.component.readableWidth
import org.koin.compose.viewmodel.koinViewModel

/**
 * The bar sits below the content, so it covers the bottom navigation bar and nothing else: a side
 * navigation bar, which landscape puts next to the content, still has to be padded for above.
 */
private val consumedInsets: WindowInsets
    @Composable get() = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)

@Composable
fun MiniPlayerScaffold(
    isAllowed: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val song = uiState.song.takeIf { isAllowed }
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .then(if (song == null) Modifier else Modifier.consumeWindowInsets(consumedInsets)),
        ) {
            content()
        }
        if (song != null) {
            // The bar is capped inside the navigation bar padding, not around it, so it centres on
            // the same axis as the content above rather than on the whole window.
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
