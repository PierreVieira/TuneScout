package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.library.presentation.viewmodel.CreatePlaylistViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePlaylistScreen(viewModel: CreatePlaylistViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CreatePlaylistContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
