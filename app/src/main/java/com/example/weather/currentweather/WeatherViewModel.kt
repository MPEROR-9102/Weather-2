package com.example.weather.currentweather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.PreferenceManager
import com.example.weather.api.ApiHelper
import com.example.weather.api.LocationResponse
import com.example.weather.api.Status
import com.example.weather.database.Location
import com.example.weather.database.LocationDao
import com.example.weather.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WeatherViewModel"

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @ApplicationScope applicationScope: CoroutineScope,
    preferenceManager: PreferenceManager,
    private val apiHelper: ApiHelper,
    private val locationDao: LocationDao,
) : ViewModel() {

    private val weatherEventsChannel = Channel<WeatherForecastEvents>()
    val weatherEvents = weatherEventsChannel.receiveAsFlow()

    private val allLocationFlow = locationDao.getAll()
    val allLocation = allLocationFlow.asLiveData()
    val preferencesFlow = preferenceManager.preferencesFlow
    val preferences = preferencesFlow.asLiveData()

    init {
//        Log.d(TAG, "Init: WeatherViewModel")

        applicationScope.launch {
            val allLocationData = allLocationFlow.first()
            if (!allLocationData.isNullOrEmpty()) {
                loadCityListWeatherData(allLocationData)
            }
            val currentLocation = preferencesFlow.first().currentLocation
            if (currentLocation.isNotBlank()) {
//                Log.e(TAG, ": ")
                loadAndDisplayWeatherData(currentLocation)
            }
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
        viewModelScope.launch {
            loadAndDisplayWeatherData(location)
        }
    }

    private suspend fun loadAndDisplayWeatherData(location: String) {
        weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.LOADING))
        try {
            val weatherData = apiHelper.currentLocation(location)
            weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.SUCCESS))
            weatherEventsChannel.send(WeatherForecastEvents.DisplayWeatherData(weatherData))
        } catch (exception: Exception) {
            weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.ERROR))
        }
    }

    private suspend fun loadCityListWeatherData(locationList: List<Location>) {
        try {
//            Log.d(TAG, "loadCityListWeatherData: Loading...")
//            Log.d(TAG, "loadCityListWeatherData: $locationList")
            for (location in locationList) {
                val locationData = apiHelper.currentLocation(location.cityName)
                locationDao.update(
                    locationData.name,
                    locationData.main.temp,
                    locationData.weather[0].main,
                    locationData.weather[0].icon
                )
//                Log.d(TAG, "loadCityListWeatherData: ${locationData.name} Updated")
            }
//            Log.d(TAG, "loadCityListWeatherData: Completed")
        } catch (exception: Exception) {
            weatherEventsChannel.send(
                WeatherForecastEvents.ShowUnableToLoadMessage(
                    "Error Loading Data"
                )
            )
        }
    }

    sealed class WeatherForecastEvents {
        object ShowCitiesScreen : WeatherForecastEvents()
        data class ShowCurrentLoadingStatus(val status: Status) : WeatherForecastEvents()
        data class DisplayWeatherData(val locationData: LocationResponse) :
            WeatherForecastEvents()

        data class ShowUnableToLoadMessage(val message: String) : WeatherForecastEvents()
    }
}