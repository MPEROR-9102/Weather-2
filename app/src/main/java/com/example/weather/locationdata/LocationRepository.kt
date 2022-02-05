package com.example.weather.locationdata

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
    suspend fun currentLocation(cityName: String) =
        apiHelper.currentLocation(cityName)

    suspend fun insertLocation(location: Location) {
        locationDao.insert(location)
    }

    suspend fun deleteLocation(cityName: String) {
        locationDao.delete(cityName)
    }

    fun getAllLocations() = locationDao.getAll()

    suspend fun deleteAllLocation() {
        locationDao.deleteAll()
    }
}