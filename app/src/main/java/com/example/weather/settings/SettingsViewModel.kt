package com.example.weather.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
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
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val preferenceFlow = preferenceManager.preferencesFlow
    val preferences = preferenceFlow.asLiveData()

    private lateinit var currentTemperatureUnit: TemperatureUnit
    private lateinit var currentSpeedUnit: SpeedUnit
    private var changedTemperatureUnit: TemperatureUnit? = null
    private var changedSpeedUnit: SpeedUnit? = null

    init {
        viewModelScope.launch {
            preferenceFlow.first().apply {
                currentTemperatureUnit = this.temperatureUnit
                currentSpeedUnit = this.speedUnit
            }
        }
    }

    fun onTemperatureToggleButtonClicked(temperatureUnit: TemperatureUnit) {
        changedTemperatureUnit = when (temperatureUnit) {
            TemperatureUnit.FAHRENHEIT -> {
                TemperatureUnit.FAHRENHEIT
            }
            TemperatureUnit.CELSIUS -> {
                TemperatureUnit.CELSIUS
            }
        }
    }

    fun onSpeedToggleButtonClicked(speedUnit: SpeedUnit) {
        changedSpeedUnit = when (speedUnit) {
            SpeedUnit.MILES -> SpeedUnit.MILES
            SpeedUnit.KILOMETERS -> SpeedUnit.KILOMETERS
        }
    }

    override fun onCleared() {
        super.onCleared()
        applicationScope.launch {
            val temperatureChange =
                changedTemperatureUnit != null && changedTemperatureUnit != currentTemperatureUnit
            if (temperatureChange) {
                Log.d(TAG, "onCleared: Temperature Changed")
                preferenceManager.updateTemperatureUnit(changedTemperatureUnit as TemperatureUnit)
            }
            val speedChange = changedSpeedUnit != null && changedSpeedUnit != currentSpeedUnit
            if (speedChange) {
                Log.d(TAG, "onCleared: Speed Changed")
                preferenceManager.updateSpeedUnit(changedSpeedUnit as SpeedUnit)
            }
            if (temperatureChange || speedChange) {
                coreActivityChannel.send(CoreActivityEvent.UndergoUnitChange)
            }
        }
    }
}