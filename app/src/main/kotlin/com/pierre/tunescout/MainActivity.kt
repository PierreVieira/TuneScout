package com.pierre.tunescout

import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierre.tunescout.presentation.content.MainContent
import com.pierre.tunescout.presentation.model.MainUiState
import com.pierre.tunescout.presentation.viewmodel.MainViewModel
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.pierre.tunescout.ui.theme.R as ThemeR

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
            setOnExitAnimationListener(::dissolveSplashIntoContent)
        }
        super.onCreate(savedInstanceState)
        reportLaunchDeepLink(savedInstanceState)
        reportSystemDarkTheme()
        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            LaunchedEffect(isSystemInDarkTheme) { viewModel.onSystemDarkThemeChanged(isSystemInDarkTheme) }
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            when (val state = uiState) {
                MainUiState.Loading -> Unit
                is MainUiState.Ready -> ThemedContent(state)
            }
        }
    }

    private fun reportLaunchDeepLink(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) viewModel.onDeepLinkReceived(intent.dataString)
    }

    /**
     * The splash holds the first frame until the state is ready, so a state that learned the
     * device's dark mode only from the composition would wait for a frame that waits for it. The
     * effect in `setContent` carries the changes that come after this one.
     */
    private fun reportSystemDarkTheme() {
        viewModel.onSystemDarkThemeChanged(resources.configuration.isSystemInDarkTheme)
    }

    /**
     * The system splash window only takes a flat colour, so the gradient of the design arrives on
     * the way out: it fades in over that colour, under the note the system already drew, and then
     * the whole splash dissolves into the screen composed behind it. Nothing waits on either fade —
     * the app is ready and drawn before the first one starts.
     */
    private fun dissolveSplashIntoContent(splash: SplashScreenViewProvider) {
        val background = TransitionDrawable(
            arrayOf(
                ColorDrawable(getColor(ThemeR.color.splash_background)),
                checkNotNull(getDrawable(ThemeR.drawable.splash_gradient)),
            ),
        )
        splash.view.background = background
        background.startTransition(GRADIENT_FADE_IN_MILLIS)
        splash.view
            .animate()
            .alpha(0f)
            .setStartDelay(GRADIENT_FADE_IN_MILLIS.toLong())
            .setDuration(SPLASH_FADE_OUT_MILLIS)
            .withEndAction(splash::remove)
    }

    /**
     * The activity is `singleTop`, so a widget tapped while the app is already open lands here
     * instead of on a second instance.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.onDeepLinkReceived(intent.dataString)
    }

    @Composable
    private fun ThemedContent(state: MainUiState.Ready) {
        LaunchedEffect(state.systemBars) {
            enableEdgeToEdge(
                statusBarStyle = state.systemBars.statusBar.style,
                navigationBarStyle = state.systemBars.navigationBar.style,
            )
        }
        TuneScoutTheme(
            theme = state.theme,
            isDynamicColorEnabled = state.isDynamicColorEnabled,
        ) {
            MainContent(
                requestNotificationPermissionsUiAction = viewModel.requestNotificationPermissionsUiAction,
                isOffline = state.isOffline,
            )
        }
    }

    private companion object {
        const val GRADIENT_FADE_IN_MILLIS = 200
        const val SPLASH_FADE_OUT_MILLIS = 200L
    }
}

/** Whether the device is in dark mode, as the resources this activity was created with report it. */
private val Configuration.isSystemInDarkTheme: Boolean
    get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
