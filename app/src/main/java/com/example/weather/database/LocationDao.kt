package com.example.weather.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("SELECT * FROM location_table")
    fun getAll(): LiveData<List<Location>>

    @Query("DELETE FROM location_table")
    suspend fun deleteAll()
}