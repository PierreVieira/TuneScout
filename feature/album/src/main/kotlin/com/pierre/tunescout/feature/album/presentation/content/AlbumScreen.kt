package com.pierre.tunescout.feature.album.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AlbumScreen(
    route: AlbumRoute,
    viewModel: AlbumViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlbumContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
