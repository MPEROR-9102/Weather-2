package com.example.weather

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

val <T> T.exhaustive: T
    get() = this

fun iconUrl(iconId: String) = "http://openweathermap.org/img/wn/$iconId@2x.png"

@RequiresApi(Build.VERSION_CODES.O)
private fun getClock(dt: Long, timeZone: String): ZonedDateTime =
    Instant.ofEpochSecond(dt).atZone(ZoneId.of(timeZone))

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(dt: Long, timeZone: String): String {
    getClock(dt, timeZone).apply {
        return "${dayOfWeek.name.lowercase().capitalize().substring(0, 3)}, " +
                month.name.lowercase().capitalize().substring(0, 3) + " " +
                toLocalDate().toString().substring(8)
    }
}

fun formatTempDisplay(temp: Float, tempDisplayUnit: TemperatureUnit): String {
    return when (tempDisplayUnit) {
        TemperatureUnit.FAHRENHEIT -> String.format("%.0f°", temp)
        TemperatureUnit.CELSIUS -> {
            val celsiusTemp = (temp - 32f) * (5f / 9f)
            String.format("%.0f°", celsiusTemp)
        }
    }
}

fun formatWindSpeedDisplay(windSpeed: Float, speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.MILES -> {
            String.format("%1$.1f mi/h", windSpeed / 1.609)
        }
        SpeedUnit.KILOMETERS -> {
            String.format("%1$1.1f km/h", windSpeed)
        }
    }
}

fun formatVisibilityDisplay(visibility: Float, speedUnit: SpeedUnit): String {
    return when (speedUnit) {
        SpeedUnit.MILES -> {
            String.format("%1$1.1f mi", visibility / 1609)
        }
        SpeedUnit.KILOMETERS -> {
            String.format("%1$1.1f km", visibility / 1000)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime(dt: Long, timeZone: String): String {
    var hour = getClock(dt, timeZone).hour
    var clock = "AM"

    if (hour >= 12)
        clock = "PM"
    if (hour > 12)
        hour -= 12
    val min = getClock(dt, timeZone).minute
    return when (min.toString().length) {
        1 -> String.format("$hour:0$min $clock")
        2 -> String.format("$hour:$min $clock")
        else -> ""
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getHour(dt: Long, timeZone: String): String {
    var hour = getClock(dt, timeZone).hour
    var clock = "AM"

    if (hour >= 12)
        clock = "PM"
    if (hour > 12)
        hour -= 12
    return "$hour $clock"
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatHourlyTime(currentTime: Long, hourlyTime: Long, timeZone: String): String {
    val current = getHour(currentTime, timeZone)
    val hourly = getHour(hourlyTime, timeZone)
    return if (current == hourly) "Now" else hourly
}

@RequiresApi(Build.VERSION_CODES.O)
private fun hourInMin(dt: Long, timeZone: String) =
    getClock(dt, timeZone).hour * 60 + getClock(dt, timeZone).minute


@RequiresApi(Build.VERSION_CODES.O)
fun getSunProgress(dt: Long, rise: Long, set: Long, timeZone: String): Int {
    val riseMin = hourInMin(rise, timeZone)
    val setMin = hourInMin(set, timeZone)
    val currentMin = getClock(dt, timeZone).hour * 60 + getClock(dt, timeZone).minute

    val estimatedInterval = setMin - riseMin
    val liveStat = currentMin - riseMin

    return when {
        currentMin <= riseMin -> 0
        (currentMin in (riseMin + 1)..setMin) -> (liveStat * 100) / estimatedInterval
        else -> 100
    }
}

private val DIRECTIONS = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
fun getDirection(degrees: Int) = DIRECTIONS[degrees / 45]

