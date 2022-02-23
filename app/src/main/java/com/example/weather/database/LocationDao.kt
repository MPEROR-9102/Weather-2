package com.example.weather.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location): Long

    @Query(
        "UPDATE location_table SET " +
                "temperature = :temp, " +
                "description = :main, " +
                "icon_id = :iconId " +
                "WHERE city_name = :cityName"
    )
    suspend fun update(cityName: String, temp: Float, main: String, iconId: String)

    @Delete
    suspend fun delete(location: Location)

    @Query("DELETE FROM location_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM location_table")
    fun getAll(): Flow<List<Location>>
}