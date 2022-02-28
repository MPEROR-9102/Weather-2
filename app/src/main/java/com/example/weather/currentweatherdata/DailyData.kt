package com.example.weather.currentweatherdata

data class DailyData(
    val timeZone: String,
    val date: Long,
    val iconId: String,
    val main: String
)