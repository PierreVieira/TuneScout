package com.pierre.tunescout.core.playback.internal

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import com.pierre.tunescout.core.model.RepeatMode
import com.pierre.tunescout.core.playback.R

/**
 * Decides which buttons the media session shows next to the transport controls, and what each says.
 *
 * Like comes first, as it always has: a surface with room for one extra button keeps showing it.
 * Shuffle and repeat follow, so a surface with more room — a car, a watch, the expanded
 * notification — can switch them without opening the app. Each icon shows the state the mode is in
 * and each label says what pressing the button does, the way the like button already worked.
 */
@OptIn(UnstableApi::class)
internal class MediaButtonSpecFactory {
    /**
     * @return the buttons for the song [favorite] is about, or none while nothing is loaded.
     */
    fun createSpecs(
        favorite: FavoriteButtonState?,
        isShuffleEnabled: Boolean,
        repeatMode: RepeatMode,
    ): List<MediaButtonSpec> {
        if (favorite == null) return emptyList()
        return listOf(
            createFavoriteSpec(favorite.isFavorite),
            createShuffleSpec(isShuffleEnabled),
            createRepeatSpec(repeatMode),
        )
    }

    private fun createFavoriteSpec(isFavorite: Boolean): MediaButtonSpec = MediaButtonSpec(
        action = TOGGLE_FAVORITE_ACTION,
        icon = if (isFavorite) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED,
        label = if (isFavorite) R.string.playback_unfavorite else R.string.playback_favorite,
    )

    private fun createShuffleSpec(isShuffleEnabled: Boolean): MediaButtonSpec = MediaButtonSpec(
        action = TOGGLE_SHUFFLE_ACTION,
        icon = if (isShuffleEnabled) CommandButton.ICON_SHUFFLE_ON else CommandButton.ICON_SHUFFLE_OFF,
        label = if (isShuffleEnabled) R.string.playback_shuffle_turn_off else R.string.playback_shuffle_turn_on,
    )

    /**
     * @return the repeat button, whose label names the mode a press leads to: the one after
     * [repeatMode].
     */
    private fun createRepeatSpec(repeatMode: RepeatMode): MediaButtonSpec = MediaButtonSpec(
        action = CYCLE_REPEAT_ACTION,
        icon = when (repeatMode) {
            RepeatMode.Off -> CommandButton.ICON_REPEAT_OFF
            RepeatMode.All -> CommandButton.ICON_REPEAT_ALL
            RepeatMode.One -> CommandButton.ICON_REPEAT_ONE
        },
        label = when (repeatMode.next) {
            RepeatMode.Off -> R.string.playback_repeat_turn_off
            RepeatMode.All -> R.string.playback_repeat_queue
            RepeatMode.One -> R.string.playback_repeat_song
        },
    )

    companion object {
        const val TOGGLE_FAVORITE_ACTION = "com.pierre.tunescout.TOGGLE_FAVORITE"
        const val TOGGLE_SHUFFLE_ACTION = "com.pierre.tunescout.TOGGLE_SHUFFLE"
        const val CYCLE_REPEAT_ACTION = "com.pierre.tunescout.CYCLE_REPEAT"
    }
}
