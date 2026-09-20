package com.pierre.tunescout.core.network

internal fun interface CountryProvider {
    fun provide(): String
}
