package com.pierre.tunescout.core.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.core.playback.internal.FavoriteButtonState
import com.pierre.tunescout.core.playback.internal.MediaButtonSpec
import com.pierre.tunescout.core.playback.internal.MediaButtonSpecFactory
import com.pierre.tunescout.core.testing.fixture.song
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(UnstableApi::class)
class MediaButtonSpecFactoryTest {
    private lateinit var factory: MediaButtonSpecFactory

    @BeforeEach
    fun setUp() {
        factory = MediaButtonSpecFactory()
    }

    @Test
    fun `GIVEN no song is loaded WHEN creating the buttons THEN there are none`() {
        // When
        val specs = factory.createSpecs(favorite = null, isShuffleEnabled = true, repeatMode = RepeatMode.All)

        // Then
        assertThat(specs).isEmpty()
    }

    @Test
    fun `GIVEN a song with every mode off WHEN creating the buttons THEN each offers to turn its mode on`() {
        // Given
        val favorite = FavoriteButtonState(song = song(id = 1), isFavorite = false)

        // When
        val specs = factory.createSpecs(favorite = favorite, isShuffleEnabled = false, repeatMode = RepeatMode.Off)

        // Then
        assertThat(specs)
            .containsExactly(
                MediaButtonSpec(
                    action = MediaButtonSpecFactory.TOGGLE_FAVORITE_ACTION,
                    icon = CommandButton.ICON_HEART_UNFILLED,
                    label = R.string.playback_favorite,
                ),
                MediaButtonSpec(
                    action = MediaButtonSpecFactory.TOGGLE_SHUFFLE_ACTION,
                    icon = CommandButton.ICON_SHUFFLE_OFF,
                    label = R.string.playback_shuffle_turn_on,
                ),
                MediaButtonSpec(
                    action = MediaButtonSpecFactory.CYCLE_REPEAT_ACTION,
                    icon = CommandButton.ICON_REPEAT_OFF,
                    label = R.string.playback_repeat_queue,
                ),
            ).inOrder()
    }

    @Test
    fun `GIVEN a liked song shuffled WHEN creating the buttons THEN each offers to take its state back`() {
        // Given
        val favorite = FavoriteButtonState(song = song(id = 1), isFavorite = true)

        // When
        val specs = factory.createSpecs(favorite = favorite, isShuffleEnabled = true, repeatMode = RepeatMode.One)

        // Then
        assertThat(specs.map { spec -> spec.icon to spec.label })
            .containsExactly(
                CommandButton.ICON_HEART_FILLED to R.string.playback_unfavorite,
                CommandButton.ICON_SHUFFLE_ON to R.string.playback_shuffle_turn_off,
                CommandButton.ICON_REPEAT_ONE to R.string.playback_repeat_turn_off,
            ).inOrder()
    }

    @Test
    fun `GIVEN the queue repeats WHEN creating the buttons THEN repeat offers the song next`() {
        // Given
        val favorite = FavoriteButtonState(song = song(id = 1), isFavorite = false)

        // When
        val repeat = factory
            .createSpecs(
                favorite = favorite,
                isShuffleEnabled = false,
                repeatMode = RepeatMode.All,
            ).last()

        // Then
        assertThat(repeat.icon).isEqualTo(CommandButton.ICON_REPEAT_ALL)
        assertThat(repeat.label).isEqualTo(R.string.playback_repeat_song)
    }
}
