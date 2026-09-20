package com.pierre.tunescout.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

class TuneScoutDataStoreFactory(
    private val context: Context,
) {
    fun create(): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.filesDir.resolve(PREFERENCES_FILE_NAME) },
    )

    private companion object {
        const val PREFERENCES_FILE_NAME = "tunescout.preferences_pb"
    }
}
