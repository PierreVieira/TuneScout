package com.pierre.tunescout.feature.audiosearch.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.audiosearch.presentation.viewmodel.AudioSearchViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AudioSearchScreen(viewModel: AudioSearchViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AudioSearchContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
