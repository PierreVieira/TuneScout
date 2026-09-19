package com.pierre.tunescout.feature.themeselection.domain.usecase.impl

import com.pierre.tunescout.feature.themeselection.domain.usecase.IsDynamicColorSupported
import com.pierre.tunescout.ui.theme.isDynamicColorSupported

class IsDynamicColorSupportedUseCase : IsDynamicColorSupported {
    override fun invoke(): Boolean = isDynamicColorSupported
}
