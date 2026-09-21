package com.pierre.tunescout.core.audiosearch

/**
 * Whether the device can turn speech into text at all. A screen asks before it offers the
 * microphone, so a device with no recognition service never shows a button that cannot work.
 */
fun interface AudioSearchAvailability {
    fun isAvailable(): Boolean
}
