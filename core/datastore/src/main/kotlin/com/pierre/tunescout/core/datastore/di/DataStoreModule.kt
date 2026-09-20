package com.pierre.tunescout.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pierre.tunescout.core.datastore.TuneScoutDataStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val dataStoreModule: Module = module {
    single { TuneScoutDataStoreFactory(context = androidContext()) }
    single<DataStore<Preferences>> { get<TuneScoutDataStoreFactory>().create() }
}
