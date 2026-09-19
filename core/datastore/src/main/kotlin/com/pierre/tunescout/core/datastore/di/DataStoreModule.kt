package com.pierre.tunescout.core.datastore.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pierre.tunescout.core.datastore.createPreferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val dataStoreModule: Module = module {
    single<DataStore<Preferences>> { createPreferencesDataStore(androidContext()) }
}
