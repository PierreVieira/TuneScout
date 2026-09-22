package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.library.presentation.viewmodel.LibraryViewModel
import com.pierre.tunescout.ui.utils.window.rememberWindowSize
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(viewModel: LibraryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryContent(
        uiState = uiState,
        isTwoPane = rememberWindowSize().isTwoPane,
        onEvent = viewModel::onEvent,
    )
}
