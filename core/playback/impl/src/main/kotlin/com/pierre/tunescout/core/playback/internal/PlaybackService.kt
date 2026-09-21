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
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.R
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.core.playback.di.PLAYBACK_SCOPE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
    private val observablePlayback: ObservablePlayback by inject()
    private val transportControls: TransportControls by inject()
    private val mediaButtonSpecFactory: MediaButtonSpecFactory by inject()
    private val scope: CoroutineScope by inject(named(PLAYBACK_SCOPE))
    private val customCommands = listOf(
        MediaButtonSpecFactory.TOGGLE_FAVORITE_ACTION,
        MediaButtonSpecFactory.TOGGLE_SHUFFLE_ACTION,
        MediaButtonSpecFactory.CYCLE_REPEAT_ACTION,
    ).map { action -> SessionCommand(action, Bundle.EMPTY) }
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
        observeMediaButtons()
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
                .addSessionCommands(customCommands)
                .build(),
        ).build()

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> = Futures.immediateFuture(
        SessionResult(
            when (customCommand.customAction) {
                MediaButtonSpecFactory.TOGGLE_FAVORITE_ACTION -> toggleCurrentFavorite()

                MediaButtonSpecFactory.TOGGLE_SHUFFLE_ACTION -> {
                    transportControls.toggleShuffle()
                    SessionResult.RESULT_SUCCESS
                }

                MediaButtonSpecFactory.CYCLE_REPEAT_ACTION -> {
                    transportControls.cycleRepeatMode()
                    SessionResult.RESULT_SUCCESS
                }

                else -> SessionResult.RESULT_ERROR_NOT_SUPPORTED
            },
        ),
    )

    /**
     * @return the session result code: the like is switched in the background, so success means it
     * was started, and an invalid state means no song is loaded to like.
     */
    private fun toggleCurrentFavorite(): Int {
        val state = currentFavoriteState ?: return SessionResult.RESULT_ERROR_INVALID_STATE
        scope.launch { toggleFavorite(state) }
        return SessionResult.RESULT_SUCCESS
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

    private fun observeMediaButtons() {
        val modes = observablePlayback
            .observePlaybackState()
            .map { state -> state.isShuffleEnabled to state.repeatMode }
            .distinctUntilChanged()
        favorites
            .observe()
            .onEach { state -> currentFavoriteState = state }
            .combine(modes) { favorite, (isShuffleEnabled, repeatMode) ->
                mediaButtonSpecFactory.createSpecs(favorite, isShuffleEnabled, repeatMode)
            }.onEach { specs -> mediaSession?.setMediaButtonPreferences(specs.map(::createCommandButton)) }
            .launchIn(scope)
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

    private fun createCommandButton(spec: MediaButtonSpec): CommandButton = CommandButton
        .Builder(spec.icon)
        .setDisplayName(getString(spec.label))
        .setSessionCommand(SessionCommand(spec.action, Bundle.EMPTY))
        .build()

    private companion object {
        const val TAG = "PlaybackService"
    }
}
