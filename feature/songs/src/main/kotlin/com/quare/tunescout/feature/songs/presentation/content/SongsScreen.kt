package com.quare.tunescout.feature.songs.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.quare.tunescout.feature.songs.presentation.viewmodel.SongsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SongsScreen(viewModel: SongsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    SongsContent(
        uiState = uiState,
        searchResults = searchResults,
        onEvent = viewModel::onEvent,
    )
}
