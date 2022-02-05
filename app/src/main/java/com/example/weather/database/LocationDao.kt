package com.example.weather.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location)

    @Query("DELETE FROM location_table WHERE city_name = :cityName")
    suspend fun delete(cityName: String)

    @Query("SELECT * FROM location_table")
    fun getAll(): LiveData<List<Location>>

    @Query("DELETE FROM location_table")
    suspend fun deleteAll()
}