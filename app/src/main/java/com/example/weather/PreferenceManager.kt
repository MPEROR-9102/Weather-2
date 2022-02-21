package com.example.weather

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val FILE_NAME = "user_preference"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(FILE_NAME)

data class FilterPreferences(
    val currentLocation: String
)

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferenceKeys {
        val CURRENT_LOCATION = stringPreferencesKey("currentLocation")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperatureUnit")
    }

    val preferencesFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            val location = it[PreferenceKeys.CURRENT_LOCATION] ?: ""
            FilterPreferences(location)
        }

    suspend fun updateCurrentLocation(cityName: String) {
        context.dataStore.edit {
            it[PreferenceKeys.CURRENT_LOCATION] = cityName
        }
    }
}