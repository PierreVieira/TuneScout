package com.pierre.tunescout.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

private const val PREFERENCES_FILE_NAME = "tunescout.preferences_pb"

fun createPreferencesDataStore(context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.create(
    produceFile = { context.filesDir.resolve(PREFERENCES_FILE_NAME) },
)
