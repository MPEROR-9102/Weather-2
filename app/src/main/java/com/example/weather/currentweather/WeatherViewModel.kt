package com.example.weather.currentweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.PreferenceManager
import com.example.weather.api.LocationResponse
import com.example.weather.api.Status
import com.example.weather.locationdata.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    preferenceManager: PreferenceManager,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val weatherEventsChannel = Channel<WeatherForecastEvents>()
    val weatherEvents = weatherEventsChannel.receiveAsFlow()

    val preferencesFlow = preferenceManager.preferencesFlow

    init {
        var location = ""
        viewModelScope.launch {
            location = preferencesFlow.first().currentLocation
        }
        if (location.isNotBlank()) {
            loadAndDisplayWeatherData(location)
        }
    }

    fun onCitiesActionClicked() {
        showCitiesScreen()
    }

    private fun showCitiesScreen() =
        viewModelScope.launch {
            weatherEventsChannel.send(WeatherForecastEvents.ShowCitiesScreen)
        }

    fun onRefreshed() {
        viewModelScope.launch {
            loadAndDisplayWeatherData(preferencesFlow.first().currentLocation)
        }
    }

    fun onCurrentLocationReceived(location: String) {
        loadAndDisplayWeatherData(location)
    }

    private fun loadAndDisplayWeatherData(location: String) =
        viewModelScope.launch {
            weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.LOADING))
            try {
                val weatherData = locationRepository.currentLocation(location)
                weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.SUCCESS))
                weatherEventsChannel.send(WeatherForecastEvents.DisplayWeatherData(weatherData))
            } catch (exception: Exception) {
                weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.ERROR))
            }
        }

    sealed class WeatherForecastEvents {
        object ShowCitiesScreen : WeatherForecastEvents()
        data class ShowCurrentLoadingStatus(val status: Status) : WeatherForecastEvents()
        data class DisplayWeatherData(val locationResponse: LocationResponse) :
            WeatherForecastEvents()
    }
}