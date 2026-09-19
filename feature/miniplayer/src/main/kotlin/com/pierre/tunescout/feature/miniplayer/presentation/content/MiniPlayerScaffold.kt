package com.pierre.tunescout.feature.miniplayer.presentation.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.miniplayer.presentation.viewmodel.MiniPlayerViewModel
import org.koin.compose.viewmodel.koinViewModel

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
                .then(
                    if (song == null) Modifier else Modifier.consumeWindowInsets(WindowInsets.navigationBars),
                ),
        ) {
            content()
        }
        if (song != null) {
            MiniPlayerContent(
                song = song,
                isPlaying = uiState.isPlaying,
                progress = uiState.progress,
                onEvent = viewModel::onEvent,
                modifier = Modifier.navigationBarsPadding(),
            )
        }
    }
}
