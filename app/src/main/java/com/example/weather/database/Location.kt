package com.example.weather.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "location_table", indices = [Index(value = ["city_name"], unique = true)])
data class Location(
    @ColumnInfo(name = "city_name") val cityName: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)