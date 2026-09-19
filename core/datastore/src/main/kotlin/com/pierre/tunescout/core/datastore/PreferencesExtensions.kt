package com.pierre.tunescout.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit

suspend fun <T> DataStore<Preferences>.write(
    key: Preferences.Key<T>,
    value: T,
) {
    edit { preferences -> preferences[key] = value }
}
