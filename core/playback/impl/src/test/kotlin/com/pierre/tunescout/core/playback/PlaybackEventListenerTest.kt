package com.pierre.tunescout.core.playback

import androidx.media3.common.Player
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.PlaybackEventListener
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PlaybackEventListenerTest {
    private var startedCount = 0
    private var stoppedCount = 0
    private var changedCount = 0
    private lateinit var listener: PlaybackEventListener

    @BeforeEach
    fun setUp() {
        listener = PlaybackEventListener(
            onPlaybackStarted = { startedCount++ },
            onPlaybackStopped = { stoppedCount++ },
            onPlaybackChanged = { changedCount++ },
        )
    }

    @Test
    fun `WHEN the player starts playing THEN playback is reported as started`() {
        // When
        listener.onIsPlayingChanged(true)

        // Then
        assertThat(startedCount).isEqualTo(1)
        assertThat(stoppedCount).isEqualTo(0)
    }

    @Test
    fun `WHEN the player stops playing THEN playback is reported as stopped`() {
        // When
        listener.onIsPlayingChanged(false)

        // Then
        assertThat(stoppedCount).isEqualTo(1)
        assertThat(startedCount).isEqualTo(0)
    }

    @Test
    fun `WHEN the playback state changes THEN the change is reported`() {
        // When
        listener.onPlaybackStateChanged(Player.STATE_READY)

        // Then
        assertThat(changedCount).isEqualTo(1)
    }

    @Test
    fun `WHEN the player moves to another item THEN the change is reported`() {
        // When
        listener.onMediaItemTransition(null, Player.MEDIA_ITEM_TRANSITION_REASON_AUTO)

        // Then
        assertThat(changedCount).isEqualTo(1)
    }

    @Test
    fun `WHEN the player error changes THEN the change is reported`() {
        // When
        listener.onPlayerErrorChanged(null)

        // Then
        assertThat(changedCount).isEqualTo(1)
    }

    @Test
    fun `GIVEN playback is running WHEN it starts and stops THEN neither report leaks into the other`() {
        // When
        listener.onIsPlayingChanged(true)
        listener.onIsPlayingChanged(false)
        listener.onIsPlayingChanged(true)

        // Then
        assertThat(startedCount).isEqualTo(2)
        assertThat(stoppedCount).isEqualTo(1)
        assertThat(changedCount).isEqualTo(0)
    }
}
