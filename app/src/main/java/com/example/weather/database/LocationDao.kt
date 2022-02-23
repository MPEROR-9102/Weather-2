package com.example.weather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location): Long

    @Query("DELETE FROM location_table WHERE city_name = :cityName")
    suspend fun delete(cityName: String)

    @Query(
        "UPDATE location_table SET " +
                "temperature = :temp, " +
                "description = :main, " +
                "icon_id = :iconId " +
                "WHERE city_name = :cityName"
    )
    fun update(cityName: String, temp: Float, main: String, iconId: String)

    @Query("SELECT * FROM location_table")
    fun getAll(): Flow<List<Location>>

    @Query("DELETE FROM location_table")
    suspend fun deleteAll()
}