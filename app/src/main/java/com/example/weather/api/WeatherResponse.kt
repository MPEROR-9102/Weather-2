package com.example.weather.api

data class WeatherResponse(
    val name: String,
    val weather: List<WeatherDetails>
) {

    data class WeatherDetails(
        val main: String,
        val icon: String
    )

}