package com.pierre.tunescout.feature.album.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumOptionsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AlbumOptionsScreen(
    route: AlbumOptionsRoute,
    viewModel: AlbumOptionsViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlbumOptionsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
