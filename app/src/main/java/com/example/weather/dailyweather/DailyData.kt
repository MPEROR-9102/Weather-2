package com.example.weather.dailyweather

data class DailyData(
    val timeZone: String,
    val date: Long,
    val temp: Float,
    val clouds: Int,
    val uvi: Float,
    val pop: Float,
    val iconId: String,
    val main: String
)