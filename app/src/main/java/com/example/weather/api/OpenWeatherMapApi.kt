package com.example.weather.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApi {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }

    @GET("data/2.5/weather")
    suspend fun currentWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String
    ): WeatherResponse
}