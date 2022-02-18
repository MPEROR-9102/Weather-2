package com.example.weather.api

import com.example.weather.locationdata.LocationDetails

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
}