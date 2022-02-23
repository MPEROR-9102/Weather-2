package com.example.weather.api

data class LocationResponse(
    val name: String,
    val main: Temperature,
    val sys: Nationality,
    val weather: List<LocationDetails>
) {
    data class Temperature(
        val temp: Float
    )

    data class Nationality(
        val country: String
    )

    data class LocationDetails(
        val main: String,
        val icon: String
    )
}