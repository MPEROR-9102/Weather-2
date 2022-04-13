package com.example.weather.currentweather

import android.util.Log
import androidx.lifecycle.*
import com.example.weather.PreferenceManager
import com.example.weather.SnackBarType
import com.example.weather.WeatherReceiver
import com.example.weather.api.ApiHelper
import com.example.weather.api.Status
import com.example.weather.api.WeatherResponse
import com.example.weather.currentweatherdata.HourlyData
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

enum class CitiesStatus {
    InitialAdd, JustCities
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    preferenceManager: PreferenceManager,
    weatherReceiver: WeatherReceiver,
    private val apiHelper: ApiHelper
) : ViewModel() {

    private val weatherEventsChannel = Channel<WeatherForecastEvents>()
    val weatherEvents = weatherEventsChannel.receiveAsFlow()

    val preferencesFlow = preferenceManager.preferencesFlow

    private val connectivityNLocationFlow = combine(
        weatherReceiver.noConnectivity.asFlow(),
        preferencesFlow
    ) { noConnectivity, preferenceFlow ->
        Pair(noConnectivity, preferenceFlow.currentLocation)
    }.flatMapLatest { (noConnectivity, location) ->
        flow { emit(ConnectivityNLocation(!noConnectivity, location)) }
    }
    val connectivityNLocation = connectivityNLocationFlow.asLiveData()

    private val _currentWeatherData: MutableLiveData<WeatherResponse> = MutableLiveData()
    val currentWeatherData: LiveData<WeatherResponse> = _currentWeatherData

    fun onInitialLoadRequestReceived(location: String) {
        viewModelScope.launch {
            loadAndDisplayWeatherData(location)
        }
    }

    fun onCitiesActionClicked() {
        showCitiesScreen(CitiesStatus.JustCities)
    }

    fun onSettingsActionClicked() {
        showSettingsScreen()
    }

    private fun showCitiesScreen(citiesStatus: CitiesStatus) =
        viewModelScope.launch {
            weatherEventsChannel.send(WeatherForecastEvents.ShowCitiesScreen(citiesStatus))
        }

    private fun showSettingsScreen() =
        viewModelScope.launch {
            weatherEventsChannel.send(WeatherForecastEvents.ShowSettingsScreen)
        }

    fun onInitialAddButtonClicked() {
        viewModelScope.launch {
            showCitiesScreen(CitiesStatus.InitialAdd)
        }
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

    fun onUnitsChanged() {
        viewModelScope.launch {
            if (preferencesFlow.first().currentLocation.isNotBlank()) {
                loadAndDisplayWeatherData(preferencesFlow.first().currentLocation)
                weatherEventsChannel.send(WeatherForecastEvents.ReloadLocationData)
            }
        }
    }

    private suspend fun loadAndDisplayWeatherData(location: String) {
        weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.LOADING))
        try {
            val locationData = apiHelper.currentWeather(location)
            weatherEventsChannel.send(WeatherForecastEvents.ShowCurrentLoadingStatus(Status.SUCCESS))
            _currentWeatherData.postValue(locationData)
            locationData.apply {
                weatherEventsChannel.send(WeatherForecastEvents.LoadHourlyForecastData(
                    hourly.map {
                        HourlyData(
                            timezone,
                            current.date,
                            it.date,
                            it.weather[0].icon,
                            it.temp
                        )
                    }
                ))
            }
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
        data class LoadHourlyForecastData(val hourlyDataList: List<HourlyData>) :
            WeatherForecastEvents()

        data class ShowCitiesScreen(val status: CitiesStatus) : WeatherForecastEvents()
        object ShowSettingsScreen : WeatherForecastEvents()
        data class ShowCurrentLoadingStatus(val status: Status) : WeatherForecastEvents()
        data class ShowWeatherMessage(val type: SnackBarType, val message: String) :
            WeatherForecastEvents()

        object ReloadLocationData : WeatherForecastEvents()
    }
}