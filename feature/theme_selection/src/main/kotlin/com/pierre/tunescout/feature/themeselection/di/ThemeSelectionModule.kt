package com.pierre.tunescout.feature.themeselection.di

import com.pierre.tunescout.feature.themeselection.data.mapper.ThemePreferenceMapper
import com.pierre.tunescout.feature.themeselection.data.mapper.ThemePreferenceMapperImpl
import com.pierre.tunescout.feature.themeselection.data.repository.ThemeSelectionRepositoryImpl
import com.pierre.tunescout.feature.themeselection.domain.repository.ThemeSelectionRepository
import com.pierre.tunescout.feature.themeselection.domain.usecase.IsDynamicColorSupported
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveDynamicColorEnabled
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveTheme
import com.pierre.tunescout.feature.themeselection.domain.usecase.SetDynamicColorEnabled
import com.pierre.tunescout.feature.themeselection.domain.usecase.SetTheme
import com.pierre.tunescout.feature.themeselection.domain.usecase.ThemeSelectionUseCases
import com.pierre.tunescout.feature.themeselection.domain.usecase.impl.IsDynamicColorSupportedUseCase
import com.pierre.tunescout.feature.themeselection.domain.usecase.impl.ObserveDynamicColorEnabledUseCase
import com.pierre.tunescout.feature.themeselection.domain.usecase.impl.ObserveThemeUseCase
import com.pierre.tunescout.feature.themeselection.domain.usecase.impl.SetDynamicColorEnabledUseCase
import com.pierre.tunescout.feature.themeselection.domain.usecase.impl.SetThemeUseCase
import com.pierre.tunescout.feature.themeselection.presentation.viewmodel.DynamicColorInfoViewModel
import com.pierre.tunescout.feature.themeselection.presentation.viewmodel.ThemeSelectionViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val themeSelectionModule: Module = module {
    factoryOf(::ThemePreferenceMapperImpl).bind<ThemePreferenceMapper>()
    singleOf(::ThemeSelectionRepositoryImpl).bind<ThemeSelectionRepository>()
    factoryOf(::ObserveThemeUseCase).bind<ObserveTheme>()
    factoryOf(::SetThemeUseCase).bind<SetTheme>()
    factoryOf(::ObserveDynamicColorEnabledUseCase).bind<ObserveDynamicColorEnabled>()
    factoryOf(::SetDynamicColorEnabledUseCase).bind<SetDynamicColorEnabled>()
    factoryOf(::IsDynamicColorSupportedUseCase).bind<IsDynamicColorSupported>()
    factoryOf(::ThemeSelectionUseCases)
    viewModelOf(::ThemeSelectionViewModel)
    viewModelOf(::DynamicColorInfoViewModel)
}
