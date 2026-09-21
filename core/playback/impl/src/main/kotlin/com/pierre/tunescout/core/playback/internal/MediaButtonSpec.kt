package com.pierre.tunescout.core.playback.internal

import androidx.annotation.StringRes

/**
 * One button the media session offers beside play, previous and next — in the notification, on the
 * lock screen, and on whatever is paired: a car, a watch, a headset.
 *
 * It is plain data so the choice of buttons can be tested without a media session; the service
 * turns each one into a `CommandButton`.
 *
 * @property action the custom session command the button sends.
 * @property icon one of the `CommandButton.ICON_*` constants.
 * @property label what the button does, which is all a screen reader or a car display has to go by.
 */
internal data class MediaButtonSpec(
    val action: String,
    val icon: Int,
    @param:StringRes val label: Int,
)
