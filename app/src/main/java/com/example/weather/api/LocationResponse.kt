package com.example.weather.api

import com.example.weather.locationdata.LocationDetails

data class LocationResponse(
    val name: String,
    val weather: List<LocationDetails>
)