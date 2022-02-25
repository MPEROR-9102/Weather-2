package com.example.weather.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(location: Location): Long

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("DELETE FROM location_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM location_table")
    fun getAll(): Flow<List<Location>>
}