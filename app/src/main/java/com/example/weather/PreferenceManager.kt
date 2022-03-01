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
    val currentLocation: String,
    val unitSystem: UnitSystem
)

enum class UnitSystem {
    IMPERIAL, METRIC
}

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferenceKeys {
        val CURRENT_LOCATION = stringPreferencesKey("currentLocation")
        val UNIT_SYSTEM = stringPreferencesKey("unitSystem")
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
            val unitSystem =
                UnitSystem.valueOf(it[PreferenceKeys.UNIT_SYSTEM] ?: UnitSystem.IMPERIAL.name)
            FilterPreferences(location, unitSystem)
        }

    suspend fun updateCurrentLocation(cityName: String) {
        context.dataStore.edit {
            it[PreferenceKeys.CURRENT_LOCATION] = cityName
        }
    }

    suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        context.dataStore.edit {
            it[PreferenceKeys.UNIT_SYSTEM] = unitSystem.name
        }
    }
}