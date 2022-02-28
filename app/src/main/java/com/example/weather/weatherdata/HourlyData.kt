package com.example.weather.weatherdata

data class HourlyData(
    val timeZone: String,
    val currentDate: Long,
    val hourlyDate: Long,
    val iconId: String,
    val temp: Float
)