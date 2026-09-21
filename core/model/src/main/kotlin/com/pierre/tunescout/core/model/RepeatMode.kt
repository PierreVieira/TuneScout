package com.pierre.tunescout.core.model

/**
 * What the player does once the queue runs out: stop, start the queue over, or keep playing the
 * song it is on. The repeat button steps through them in this order.
 */
enum class RepeatMode {
    Off,
    All,
    One,
    ;

    val next: RepeatMode
        get() = entries[(ordinal + 1) % entries.size]
}
