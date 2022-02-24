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

    suspend fun currentWeather(cityName: String): WeatherResponse {
        val locationResponse = currentLocation(cityName)
        val weatherResponse = openWeatherMapApi.currentWeather(
            locationResponse.coord.lat,
            locationResponse.coord.lon,
            BuildConfig.OPEN_WEATHER_MAP_API_KEY,
            "minutely,alerts",
            "imperial"
        )
        weatherResponse.name = locationResponse.name
        weatherResponse.country = locationResponse.sys.country
        return weatherResponse
    }
}