package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.library.presentation.viewmodel.LibrarySearchViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibrarySearchScreen(viewModel: LibrarySearchViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LibrarySearchContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
