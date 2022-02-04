package com.example.weather.data

import com.example.weather.api.ApiHelper
import com.example.weather.database.Location
import com.example.weather.database.LocationDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val locationDao: LocationDao
) {
    suspend fun currentWeather(cityName: String) =
        apiHelper.currentWeather(cityName)

    suspend fun insertLocation(location: Location) {
        locationDao.insert(location)
    }

    fun getAllLocations() = locationDao.getAll()
}