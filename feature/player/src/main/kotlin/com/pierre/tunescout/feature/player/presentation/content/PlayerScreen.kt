package com.pierre.tunescout.feature.player.presentation.content

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.feature.player.presentation.model.PlayerLayout
import com.pierre.tunescout.feature.player.presentation.model.PlayerUiAction
import com.pierre.tunescout.feature.player.presentation.viewmodel.PlayerViewModel
import com.pierre.tunescout.ui.component.SnackbarBox
import com.pierre.tunescout.ui.utils.ActionCollector
import com.pierre.tunescout.ui.utils.animation.SharedArtworkDestinationEffect
import com.pierre.tunescout.ui.utils.navigation.LocalIsInDetailPane
import com.pierre.tunescout.ui.utils.window.rememberWindowSize
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The player beside the tabs on a wide window, which takes the mini player's place: it follows
 * whatever is playing, and shows an empty state until something is. It is not a screen of the back
 * stack, so there is nothing for it to go back to.
 */
@Composable
fun NowPlayingScreen() {
    PlayerScreen(songId = null)
}

/**
 * @param songId the song the player was opened on, or null for the [NowPlayingScreen].
 */
@Composable
fun PlayerScreen(
    songId: Long?,
    viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(songId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    SharedArtworkDestinationEffect()
    ActionCollector(viewModel.uiAction) { action ->
        when (action) {
            is PlayerUiAction.ShowSnackBar -> snackbarHostState.showSnackbar(resources.getString(action.message))
        }
    }
    val windowSize = rememberWindowSize()
    SnackbarBox(hostState = snackbarHostState) {
        PlayerContent(
            uiState = uiState,
            layout = PlayerLayout.of(windowSize = windowSize, isInDetailPane = LocalIsInDetailPane.current),
            hasBack = songId != null,
            onEvent = viewModel::onEvent,
        )
    }
}
