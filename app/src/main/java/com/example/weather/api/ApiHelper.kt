package com.example.weather.api

import com.example.weather.BuildConfig
import com.example.weather.PreferenceManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ApiHelper @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val openWeatherMapApi: OpenWeatherMapApi
) {

    suspend fun currentLocation(cityName: String) =
        openWeatherMapApi.currentLocation(
            cityName,
            preferenceManager.preferencesFlow.first().unitSystem.name.lowercase(),
            BuildConfig.OPEN_WEATHER_MAP_API_KEY
        )

    suspend fun currentLocation(lat: Double, lon: Double) =
        openWeatherMapApi.currentLocation(
            lat,
            lon,
            preferenceManager.preferencesFlow.first().unitSystem.name.lowercase(),
            BuildConfig.OPEN_WEATHER_MAP_API_KEY
        )

    suspend fun currentWeather(cityName: String): WeatherResponse {
        val locationResponse = currentLocation(cityName)
        val weatherResponse = openWeatherMapApi.currentWeather(
            locationResponse.coord.lat,
            locationResponse.coord.lon,
            BuildConfig.OPEN_WEATHER_MAP_API_KEY,
            "minutely,alerts",
            preferenceManager.preferencesFlow.first().unitSystem.name.lowercase()
        )
        weatherResponse.name = locationResponse.name
        weatherResponse.country = locationResponse.sys.country
        return weatherResponse
    }
}