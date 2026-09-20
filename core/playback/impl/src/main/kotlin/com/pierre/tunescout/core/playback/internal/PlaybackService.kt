package com.pierre.tunescout.core.playback.internal

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.pierre.tunescout.core.playback.R
import com.pierre.tunescout.core.playback.di.PLAYBACK_SCOPE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

@OptIn(UnstableApi::class)
internal class PlaybackService :
    MediaSessionService(),
    MediaSession.Callback {
    private val player: ExoPlayer by inject()
    private val favorites: PlaybackFavoriteController by inject()
    private val scope: CoroutineScope by inject(named(PLAYBACK_SCOPE))
    private val toggleFavoriteCommand = SessionCommand(TOGGLE_FAVORITE_ACTION, Bundle.EMPTY)
    private var currentFavoriteState: FavoriteButtonState? = null
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(this).apply { setSmallIcon(R.drawable.ic_playback_notification) },
        )
        mediaSession = MediaSession
            .Builder(this, player)
            .setCallback(this)
            .apply { createLaunchIntent()?.let(::setSessionActivity) }
            .build()
            .also(::addSession)
        observeFavoriteState()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        pauseAllPlayersAndStopSelf()
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult = MediaSession.ConnectionResult
        .AcceptedResultBuilder(session, controller)
        .setAvailableSessionCommands(
            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                .buildUpon()
                .add(toggleFavoriteCommand)
                .build(),
        ).build()

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        if (customCommand.customAction != TOGGLE_FAVORITE_ACTION) {
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
        }
        val state = currentFavoriteState
            ?: return Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_INVALID_STATE))
        scope.launch { toggleFavorite(state) }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    private fun createLaunchIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun observeFavoriteState() {
        favorites
            .observe()
            .onEach { state ->
                currentFavoriteState = state
                mediaSession?.setMediaButtonPreferences(buildFavoriteMediaButtons(state))
            }.launchIn(scope)
    }

    private suspend fun toggleFavorite(state: FavoriteButtonState) {
        try {
            favorites.toggle(state)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Could not toggle favorite for song ${state.song.id}: ${exception.message}")
        }
    }

    private fun buildFavoriteMediaButtons(state: FavoriteButtonState?): List<CommandButton> {
        if (state == null) return emptyList()
        val icon = if (state.isFavorite) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED
        val label = getString(if (state.isFavorite) R.string.playback_unfavorite else R.string.playback_favorite)
        return listOf(
            CommandButton
                .Builder(icon)
                .setDisplayName(label)
                .setSessionCommand(toggleFavoriteCommand)
                .build(),
        )
    }

    private companion object {
        const val TAG = "PlaybackService"
        const val TOGGLE_FAVORITE_ACTION = "com.pierre.tunescout.TOGGLE_FAVORITE"
    }
}
