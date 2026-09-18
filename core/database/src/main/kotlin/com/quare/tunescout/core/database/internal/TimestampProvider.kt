package com.quare.tunescout.core.database.internal

internal fun interface TimestampProvider {
    fun provide(): Long
}
