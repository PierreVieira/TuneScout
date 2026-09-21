package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiAction
import com.pierre.tunescout.feature.songs.presentation.viewmodel.SongsViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import com.pierre.tunescout.ui.utils.window.rememberWindowSize
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SongsScreen(viewModel: SongsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is SongsUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    SnackbarBox(hostState = snackbarHostState) {
        SongsContent(
            uiState = uiState,
            searchResults = searchResults,
            isHeaderInline = rememberWindowSize().isSideBySide,
            onEvent = viewModel::onEvent,
        )
    }
}
