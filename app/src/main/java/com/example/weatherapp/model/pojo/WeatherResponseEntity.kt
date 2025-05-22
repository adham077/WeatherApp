package com.example.weatherapp.model.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.weatherapp.model.data.source.local.WeatherTypeConverters

@Entity(tableName = "weather_table")
data class WeatherResponseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @TypeConverters(WeatherTypeConverters::class)
    val response : WeatherTimed
)