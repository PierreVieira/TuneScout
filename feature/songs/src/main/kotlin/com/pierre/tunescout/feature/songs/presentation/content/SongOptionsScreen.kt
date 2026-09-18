package com.pierre.tunescout.feature.songs.presentation.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.feature.songs.presentation.viewmodel.SongOptionsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SongOptionsScreen(
    route: SongOptionsRoute,
    viewModel: SongOptionsViewModel = koinViewModel(parameters = { parametersOf(route) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SongOptionsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
