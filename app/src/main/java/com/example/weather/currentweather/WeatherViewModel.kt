package com.example.weather.currentweather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.PreferenceManager
import com.example.weather.SnackBarType
import com.example.weather.WeatherReceiver
import com.example.weather.api.ApiHelper
import com.example.weather.api.Status
import com.example.weather.api.WeatherResponse
import com.example.weather.location.SendType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WeatherViewModel"

data class ConnectivityNLocation(
    val network: Boolean,
    val location: String
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    preferenceManager: PreferenceManager,
    weatherReceiver: WeatherReceiver,
    private val apiHelper: ApiHelper
) : ViewModel() {

    private val weatherEventsChannel = Channel<WeatherForecastEvents>()
    val weatherEvents = weatherEventsChannel.receiveAsFlow()

    private val preferencesFlow = preferenceManager.preferencesFlow
    val preferences = preferencesFlow.asLiveData()

    private val connectivityNLocationFlow = combine(
        weatherReceiver.noConnectivity.asFlow(),
        preferencesFlow
    ) { noConnectivity, preferenceFlow ->
        Pair(noConnectivity, preferenceFlow.currentLocation)
    }.flatMapLatest { (noConnectivity, location) ->
        flow { emit(ConnectivityNLocation(!noConnectivity, location)) }
    }
    val connectivityNLocation = connectivityNLocationFlow.asLiveData()

    fun onInitialLoadRequestReceived(location: String) {
        viewModelScope.launch {
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

    fun onCurrentLocationReceived(sendType: SendType, location: String) {
        when (sendType) {
            SendType.ADD -> {
                setCurrentLocation(location)
            }
            SendType.UPDATE -> {
                updateCurrentWeather(location)
            }
        }
    }

    private fun updateCurrentWeather(location: String) =
        viewModelScope.launch {
            loadAndDisplayWeatherData(location)
        }

    private fun setCurrentLocation(location: String) =
        viewModelScope.launch {
            loadAndDisplayWeatherData(location)
            weatherEventsChannel.send(
                WeatherForecastEvents.ShowWeatherMessage(
                    SnackBarType.WHITE,
                    "$location Added"
                )
            )
        }

    private suspend fun loadAndDisplayWeatherData(location: String) {
        weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.LOADING))
        try {
            val locationData = apiHelper.currentWeather(location)
            weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.SUCCESS))
            weatherEventsChannel.send(WeatherForecastEvents.DisplayWeatherData(locationData))
            Log.d(TAG, "loadAndDisplayWeatherData: Loaded Successfully")
        } catch (exception: Exception) {
            weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.ERROR))
            Log.d(TAG, "loadAndDisplayWeatherData: ${exception.message}")
            when (exception.message) {
                "timeout" ->
                    WeatherForecastEvents.ShowWeatherMessage(
                        SnackBarType.RED,
                        "Took Too Long To Respond"
                    )
                else ->
                    WeatherForecastEvents.ShowWeatherMessage(
                        SnackBarType.RED,
                        "Something Went Wrong"
                    )
            }
        }
    }

    sealed class WeatherForecastEvents {
        object ShowCitiesScreen : WeatherForecastEvents()
        data class ShowCurrentLoadingStatus(val status: Status) : WeatherForecastEvents()
        data class DisplayWeatherData(val weatherData: WeatherResponse) :
            WeatherForecastEvents()

        data class ShowWeatherMessage(val type: SnackBarType, val message: String) :
            WeatherForecastEvents()
    }
}