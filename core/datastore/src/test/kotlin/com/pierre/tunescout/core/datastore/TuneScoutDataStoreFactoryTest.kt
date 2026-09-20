package com.pierre.tunescout.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TuneScoutDataStoreFactoryTest {
    @TempDir
    lateinit var filesDir: File

    @Test
    fun `GIVEN a created store WHEN writing a value THEN it is kept in the app files directory`() = runTest {
        // Given
        val context = mockk<Context> { every { filesDir } returns this@TuneScoutDataStoreFactoryTest.filesDir }
        val dataStore = TuneScoutDataStoreFactory(context = context).create()
        val key = stringPreferencesKey("theme")

        // When
        dataStore.write(key = key, value = "dark")

        // Then
        assertThat(dataStore.data.first()[key]).isEqualTo("dark")
        assertThat(File(filesDir, "tunescout.preferences_pb").exists()).isTrue()
    }
}
