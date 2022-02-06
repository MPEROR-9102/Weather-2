package com.example.weather.api

import com.example.weather.BuildConfig
import javax.inject.Inject

class ApiHelper @Inject constructor(private val openWeatherMapApi: OpenWeatherMapApi) {
    suspend fun currentLocation(cityName: String) =
        openWeatherMapApi.currentLocation(
            cityName,
            "imperial",
            BuildConfig.OPEN_WEATHER_MAP_API_KEY
        )
}