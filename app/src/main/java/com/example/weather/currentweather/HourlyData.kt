package com.example.weather.currentweather

data class HourlyData(
    val timeZone: String,
    val currentDate: Long,
    val hourlyDate: Long,
    val iconId: String,
    val temp: Float
)