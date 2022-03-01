package com.example.weather.settings

import android.util.Log
import androidx.lifecycle.*
import com.example.weather.*
import com.example.weather.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val preferenceManager: PreferenceManager,
    weatherReceiver: WeatherReceiver
) : ViewModel() {

    private val preferenceFlow = preferenceManager.preferencesFlow
    val preferences = preferenceFlow.asLiveData()

    private lateinit var currentUnitSystem: UnitSystem
    private var changedUnitSystem: UnitSystem? = null

    val connectivityLiveData = weatherReceiver.noConnectivity.switchMap {
        liveData {
            emit(!it)
        }
    }

    init {
        viewModelScope.launch {
            preferenceFlow.first().apply {
                currentUnitSystem = this.unitSystem
            }
        }
    }

    fun onUnitToggleButtonClicked(unitSystem: UnitSystem) {
        changedUnitSystem = when (unitSystem) {
            UnitSystem.IMPERIAL -> UnitSystem.IMPERIAL
            UnitSystem.METRIC -> UnitSystem.METRIC
        }
    }

    override fun onCleared() {
        super.onCleared()
        applicationScope.launch {
            if (changedUnitSystem != null && changedUnitSystem != currentUnitSystem) {
                Log.d(TAG, "onCleared: Units Changed")
                preferenceManager.updateUnitSystem(changedUnitSystem as UnitSystem)
                coreActivityChannel.send(CoreActivityEvent.UndergoUnitChange)
            }
        }
    }
}