package com.calcyoulater.app

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vault_data")

class DataStoreManager(private val context: Context) {

    companion object {
        private val FILES_KEY = stringSetPreferencesKey("files")
    }

    // Function to save files to DataStore
    suspend fun saveFiles(files: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[FILES_KEY] = files
        }
    }

    // Function to get files from DataStore
    fun getFiles(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[FILES_KEY] ?: emptySet()
        }
    }
}
