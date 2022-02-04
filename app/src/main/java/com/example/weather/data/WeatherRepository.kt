package com.example.weather.data

import com.example.weather.api.ApiHelper
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val apiHelper: ApiHelper) {
    suspend fun currentWeather(cityName: String) =
        apiHelper.currentWeather(cityName)
}