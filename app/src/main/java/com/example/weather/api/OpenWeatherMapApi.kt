package com.example.weather.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApi {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }

    @GET("data/2.5/weather")
    suspend fun currentLocation(
        @Query("q") cityName: String,
        @Query("units") unit: String,
        @Query("appid") apiKey: String
    ): LocationResponse

    @GET("data/2.5/weather")
    suspend fun currentLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("units") unit: String,
        @Query("appid") apiKey: String
    ): LocationResponse

    @GET("data/2.5/onecall")
    suspend fun currentWeather(
        @Query("lat") latitude: Float,
        @Query("lon") longitude: Float,
        @Query("appid") apiKey: String,
        @Query("exclude") exclude: String,
        @Query("units") unit: String
    ): WeatherResponse
}