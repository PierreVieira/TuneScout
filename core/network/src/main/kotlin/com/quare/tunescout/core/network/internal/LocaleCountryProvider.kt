package com.quare.tunescout.core.network.internal

import com.quare.tunescout.core.network.CountryProvider
import java.util.Locale

internal class LocaleCountryProvider : CountryProvider {
    override fun provide(): String = Locale.getDefault().country.ifBlank { DEFAULT_COUNTRY }

    private companion object {
        const val DEFAULT_COUNTRY = "US"
    }
}
