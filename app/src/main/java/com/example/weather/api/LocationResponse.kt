package com.example.weather.api

data class LocationResponse(
    val name: String,
    val main: Temperature,
    val sys: Nationality,
    val coord: Coordinates,
    val weather: List<LocationDetails>
) {
    data class Coordinates(
        val lon: Float,
        val lat: Float
    )

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